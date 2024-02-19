package com.wave.domain;

import com.wave.dto.type.ERole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "total_donation")
    private int totalDonation;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "serial_id", nullable = false, unique = true)
    private String serialId;

    @Column(name = "is_login", columnDefinition = "TINYINT(1)")
    private Boolean isLogin;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ERole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DonationCountry> donations = new ArrayList<>();

    @Builder
    private User(String nickname, int totalDonation, String refreshToken, String serialId, ERole role, LocalDateTime createdAt) {
        this.nickname = nickname;
        this.totalDonation = totalDonation;
        this.refreshToken = refreshToken;
        this.serialId = serialId;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User createUser(String nickname, String serialId, ERole role) {
        return User.builder()
                .nickname(nickname)
                .totalDonation(0)
                .serialId(serialId)
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateRefreshTokenAndLoginStatus(String refreshToken, Boolean isLogin) {
        this.refreshToken = refreshToken;
        this.isLogin = isLogin;
    }

    public void updateTotalDonation(int donation) {
        this.totalDonation += donation;
    }

    public void addDonation(DonationCountry donationCountry) {
        donations.add(donationCountry);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
