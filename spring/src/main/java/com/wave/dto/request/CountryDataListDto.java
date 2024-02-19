package com.wave.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CountryDataListDto(

        List<CountryDataDto> countryDataList
) {
        @Builder
        public record CountryDataDto(

                @NotNull(message = "id은 필수값입니다.")
                Integer id,

                @NotNull(message = "koreanName은 필수값입니다.")
                String koreanName,

                @NotNull(message = "englishName은 필수값입니다.")
                String imageUrl,

                @NotNull(message = "detailImageUrl 필수값입니다.")
                String detailImageUrl,

                @NotNull(message = "name은 필수값입니다.")
                String name,

                @NotNull(message = "category은 필수값입니다.")
                String category,

                @NotNull(message = "isDonate은 필수값입니다.")
                Boolean isDonate, // JSON 문자열을 Boolean으로 변경하였습니다.

                Integer casualties,

                @NotNull(message = "mainTitle은 필수값입니다.")
                String mainTitle,

                @NotNull(message = "subTitle은 필수값입니다.")
                String subTitle,

                @NotNull(message = "imageProducer은 필수값입니다.")
                String imageProducer,

                @NotNull(message = "detailImageTitle은 필수값입니다.")
                String detailImageTitle,

                @NotNull(message = "detailImageProducer은 필수값입니다.")
                String detailImageProducer,

                @NotNull(message = "content1은 필수값입니다.")
                String title1,

                @NotNull(message = "content1은 필수값입니다.")
                String content1,

                @NotNull(message = "title2은 필수값입니다.")
                String title2,

                @NotNull(message = "content2은 필수값입니다.")
                String content2
        ) {

        }
}
