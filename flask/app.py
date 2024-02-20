from flask import Flask, request, jsonify, Response
import requests
from bs4 import BeautifulSoup
import re
from fuzzywuzzy import fuzz
import pandas as pd
from tqdm import tqdm
import json
from config import CLIENT_ID, CLIENT_SECRET, LOCAL_SERVER, PROD_SERVER
from apscheduler.schedulers.background import BackgroundScheduler
import logging
import random
import urllib.request
from urllib.parse import quote

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

app = Flask(__name__)
country_ids = {
    "우간다": 165,
    "우크라이나": 166,
    "베네수엘라": 170,
    "예멘": 173,
    "아프가니스탄": 1,
    "부르키나파소": 14,
    "콩고": 33,
    "콜롬비아": 35,
    "에티오피아": 51,
    "아이티": 70,
    "이라크": 76,
    "케냐": 84,
    "레바논": 91,
    "스리랑카": 94,
    "말리": 104,
    "미얀마": 105,
    "모잠비크": 108,
    "니제르": 114,
    "파키스탄": 122,
    "팔레스타인": 132,
    "남수단": 140,
    "소말리아": 146,
    "시리아": 153

}

user_agents = [
    ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
     "AppleWebKit/537.36 (KHTML, like Gecko) "
     "Chrome/58.0.3029.110 Safari/537.3"),
    ("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:55.0) "
     "Gecko/20100101 Firefox/55.0"),
    ("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
     "AppleWebKit/605.1.15 (KHTML, like Gecko) "
     "Version/14.0.3 Safari/605.1.15"),
]


def translate_text_ko_to_en(text):
    Source_Lang = 'ko'
    Target_Lang = 'en'

    headers = {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'X-Naver-Client-Id': CLIENT_ID,
        'X-Naver-Client-Secret': CLIENT_SECRET,
    }

    data = f'source={Source_Lang}&target={Target_Lang}&text={text}'.encode()
    response = requests.post('https://openapi.naver.com/v1/papago/n2mt', headers=headers, data=data)
    return response.json()['message']['result']['translatedText']


# 페이지 번호 생성 함수
def make_pg_num(num):
    if num == 1:
        return num
    elif num == 0:
        return num + 1
    else:
        return num + 9 * (num - 1)


# URL 생성 함수
def make_url(search, start_pg, end_pg):
    urls = []
    for i in range(start_pg, end_pg + 1):
        page = make_pg_num(i)
        url = "https://search.naver.com/search.naver?where=news&sm=tab_pge&query=" + search + "&start=" + str(page)
        urls.append(url)
    return urls


# 속성 추출 함수
def news_attrs_crawler(articles, attrs):
    attrs_content = []
    for i in articles:
        attrs_content.append(i.attrs[attrs])
    return attrs_content


# 기사 크롤링 함수
def articles_crawler(url):
    headers = {
        "User-Agent": random.choice(user_agents)
    }
    safe_url = quote(url, safe='/:?=&')
    print("crawling url is this ====")
    req = urllib.request.Request(safe_url, headers=headers)
    with urllib.request.urlopen(req) as response:
        html = response.read().decode('utf-8')

    soup = BeautifulSoup(html, "html.parser")
    articles = soup.select("div.group_news > ul.list_news > li div.news_area > div.news_info > div.info_group > a.info")
    # print(articles)
    if not articles:
        print("no articles found")
    return news_attrs_crawler(articles, 'href')


def make_list(newlist, content):
    for i in content:
        for j in i:
            newlist.append(j)
    return newlist


def remove_escape_chars(text):
    escape_chars = ['\\', '\"', "\'"]
    for char in escape_chars:
        text = text.replace(char, '')
    return text


# 첫 데이터를 넣기 위한 api
@app.route('/flask/crawling', methods=['GET'])
def crawling():
    scheduled_crawling()
    return "Crawling and processing completed for all countries."


def scheduled_crawling():
    for country_name, country_id in country_ids.items():
        result_data = {}
        print(country_name, country_id)
        try:
            urls = make_url(country_name, 1, 2)
            news_dates = []
            news_urls = []
            news_image_url = []
            news_title = []
            news_url_1 = []

            if not urls:
                pass
            for url in urls:
                crawled_urls = articles_crawler(url)
                news_urls.append(crawled_urls)

            # 1차원 리스트로 만들기(내용 제외)
            make_list(news_url_1, news_urls)

            # NAVER 뉴스만 남기기
            final_urls = []
            for i in tqdm(range(len(news_url_1))):
                if "news.naver.com" in news_url_1[i]:
                    final_urls.append(news_url_1[i])
                else:
                    pass

            # 네이버 뉴스 URL만 필터링 및 제목, 이미지 URL, 날짜 크롤링
            if not final_urls:
                pass
            for url in final_urls:
                # print((len(final_urls)))
                headers = {
                    "User-Agent": random.choice(user_agents)
                }
                news_html = requests.get(url, headers).text
                soup = BeautifulSoup(news_html, 'html.parser')

                # 제목
                title = soup.select_one("#ct > div.media_end_head.go_trans > div.media_end_head_title > h2")
                if title is None:
                    title = soup.select_one("#content > div.end_ct > div > h2")

                # 이미지
                image = soup.select("img#img1")
                if not image:
                    image = ['none']

                pattern1 = '<[^>]*>'
                title = re.sub(pattern=pattern1, repl='', string=str(title))
                news_title.append(title)
                if image != ['none']:
                    pattern_img = r'data-src="(.*?)"'
                    image[0] = str(image[0])
                    match = re.search(pattern_img, image[0])
                    if match is not None:
                        data_src = match.group(1)
                        news_image_url.append(data_src)
                    else:
                        news_image_url.append('none')
                else:
                    news_image_url.append('해당 기사는 사진이 없습니다')

                # 날짜
                try:
                    html_date = soup.select_one(
                        "div#ct> div.media_end_head.go_trans > div.media_end_head_info.nv_notrans > div.media_end_head_info_datestamp > div > span")
                    news_date = html_date.attrs['data-date-time']
                except AttributeError:
                    news_date = soup.select_one("#content > div.end_ct > div > div.article_info > span > em")
                    news_date = re.sub(pattern='<[^>]*>', repl='', string=str(news_date))

                news_dates.append(news_date)

            # DataFrame 생성
            news_df = pd.DataFrame(
                {'date': news_dates, 'title': news_title, 'link': final_urls, 'image_url': news_image_url})
            news_df = news_df.drop_duplicates(keep='first', ignore_index=True)
            news_df = news_df[news_df['image_url'] != '해당 기사는 사진이 없습니다.']

            search_similarity = lambda x: fuzz.token_set_ratio(country_name, x)
            news_df['search_similarity'] = news_df['title'].apply(search_similarity)
            news_df = news_df.sort_values(by='search_similarity', ascending=False).reset_index(drop=True)

            if len(news_df) > 5:
                real_use5_df = news_df[:5]
            else:
                real_use5_df = news_df
            print("뉴스 프레임 생성")

            real_use5_df = real_use5_df.copy()
            real_use5_df['title'] = real_use5_df['title'].apply(lambda x: x.strip())  # 공백 없애기 -> 파파고 api 사용을 위한 전처리
            real_use5_df['title'] = real_use5_df['title'].apply(
                translate_text_ko_to_en)  # api호출해서 번역 실행하기 , 현재 title에 적용함
            real_use5_df['title'] = real_use5_df['title'].apply(remove_escape_chars)

            logging.info("Before adding data to result_data: %s", json.dumps(result_data, ensure_ascii=False))

            if not real_use5_df.empty:
                result_data = {
                    "id": country_id,
                    "data": real_use5_df.to_dict(orient='records')
                }
                logging.info(
                    f"Sending data to Spring application for {country_name}: {json.dumps(result_data, ensure_ascii=False)}")
                url = LOCAL_SERVER
                headers = {'Content-Type': 'application/json; charset=utf-8'}
                response = requests.post(url, json=result_data, headers=headers)
                if response.status_code != 200:
                    logging.error(f"Failed to send data for {country_name}. HTTP Status Code: {response.status_code}")
            else:
                logging.info(f"No data to send for {country_name}. Skipping.")

        except Exception as e:
            logging.error(f"An error occurred during processing {country_name}: {e}")


scheduler = BackgroundScheduler()
scheduler.add_job(scheduled_crawling, 'cron', hour=0, minute=0)
scheduler.start()

if __name__ == '__main__':
    app.run()
