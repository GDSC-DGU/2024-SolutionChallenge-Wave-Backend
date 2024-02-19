package com.wave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wave.dto.type.ErrorCode;
import com.wave.exception.CommonException;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Builder
@Slf4j
public record CrawlingDto(
        @NotNull
        @JsonProperty("id")
        Long id,

        @JsonProperty("data")
        List<NewsDto> data
) {
    @Builder
    public record NewsDto(

            @JsonProperty("image_url")
            @NotNull(message = "newsImageUrl는 필수값입니다.")
            String newsImageUrl,

            @JsonProperty("title")
            @NotNull(message = "newsTitle는 필수값입니다.")
            String newsTitle,

            @JsonProperty("link")
            @NotNull(message = "newsUrl는 필수값입니다.")
            String newsUrl,

            @JsonProperty("date")
            @NotNull(message = "date는 필수값입니다.")
            String date
    ) {
        public LocalDate getParsedDate() {
            String pattern = this.date.contains(".") ? "yyyy.MM.dd. hh:mm" : "yyyy-MM-dd HH:mm:ss";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                LocalDate parsedDate;
                if (this.date.contains(".")) {
                    parsedDate = LocalDate.parse(this.date.split(" ")[0], DateTimeFormatter.ofPattern("yyyy.MM.dd."));
                } else {
                    parsedDate = LocalDate.parse(this.date, formatter);
                }
                return parsedDate;
            } catch (DateTimeParseException e) {
                log.info("날짜 파싱 실패: " + this.date);
                throw new CommonException(ErrorCode.INVALID_CRAWLING_DATE);
            }
        }
    }
}
