package com.wave.dto.response;

import com.wave.domain.User;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserInfoDto(

        @NotNull(message = "id는 필수값입니다.")
        int id,

        @NotNull(message = "nickname은 필수값입니다.")
        String nickname,

        @NotNull(message = "totalWave은 필수값입니다.")
        int totalWave,

        @NotNull(message = "donationCountryCnt은 필수값입니다.")
        int donationCountryCnt
) {
    public static UserInfoDto of(User user) {
        return UserInfoDto.builder()
                .id(user.getId().intValue())
                .nickname(user.getNickname())
                .donationCountryCnt(user.getDonations().size())
                .totalWave(user.getTotalDonation())
                .build();
    }
}
