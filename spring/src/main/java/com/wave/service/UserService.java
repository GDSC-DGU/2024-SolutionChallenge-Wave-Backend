package com.wave.service;

import com.wave.domain.Country;
import com.wave.domain.DonationCountry;
import com.wave.domain.User;
import com.wave.dto.request.UserDonateDto;
import com.wave.dto.response.UserDonateInfoDto;
import com.wave.dto.response.UserInfoDto;
import com.wave.dto.type.ErrorCode;
import com.wave.exception.CommonException;
import com.wave.repository.CountryRepository;
import com.wave.repository.DonationCountryRepository;
import com.wave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final DonationCountryRepository donationCountryRepository;

    //2.1 기부하기
    @Transactional
    public void donate(UserDonateDto userDonateDto, Long userId) {
        Country country = countryRepository.findById(userDonateDto.countryId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_COUNTRY));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        donationCountryRepository.findByUserAndCountry(user, country)
                .ifPresentOrElse(
                        donationCountry -> {
                            donationCountry.updateDonation(userDonateDto.money());
                        },
                        () -> {
                            donationCountryRepository.save(DonationCountry.createDonationCountry(country, user, userDonateDto.money()));
                        }
                );
        user.updateTotalDonation(userDonateDto.money());
        country.updateWave(userDonateDto.money());
    }



    //3.1 유저 정보
    public UserInfoDto getUserInfo(Long userId) {
        User user = userRepository.findWithDonationCountryById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        return UserInfoDto.of(user);
    }

    //3.2 상세 기부 목록
    public UserDonateInfoDto getUserDonateList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        List<DonationCountry> donationCountryList = donationCountryRepository.findByUser(user);
        return UserDonateInfoDto.of(donationCountryList, user.getTotalDonation());

    }
}
