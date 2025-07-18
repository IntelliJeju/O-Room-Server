package com.oroom.user.service;

import com.oroom.security.JwtTokenProvider;
import com.oroom.user.domain.User;
import com.oroom.user.dto.KakaoTokenDTO;
import com.oroom.user.dto.KakaoUserDTO;
import com.oroom.user.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponseDTO kakaoLogin(String code) {
        try {
            log.info("1. 인가 코드로 액세스 토큰 요청 시작");

            // 1. 인가 코드로 액세스 토큰 요청
            KakaoTokenDTO tokenDto = kakaoOAuthService.getAccessToken(code);
            log.info("1. 액세스 토큰 요청 완료: {}", tokenDto.getAccessToken());

            log.info("2. 액세스 토큰으로 사용자 정보 요청 시작");

            // 2. 액세스 토큰으로 사용자 정보 요청
            KakaoUserDTO kakaoUserDto = kakaoOAuthService.getUserInfo(tokenDto.getAccessToken());
            log.info("2. 사용자 정보 요청 완료: {}", kakaoUserDto.getId());

            log.info("3. 사용자 정보 추출 시작");

            // 3. 사용자 정보 추출
            String email = kakaoUserDto.getKakaoAccount().getEmail();
            String nickname = kakaoUserDto.getKakaoAccount().getProfile().getNickname();
            String profileImage = kakaoUserDto.getKakaoAccount().getProfile().getProfileImageUrl();
            String kakaoUserId = kakaoUserDto.getId().toString();

            log.info("3. 사용자 정보 추출 완료 - email: {}, nickname: {}", email, nickname);

            // 4. 기존 사용자 조회 또는 신규 사용자 생성
            User user = userService.findByEmail(email);
            if (user == null) {
                user = User.builder()
                        .email(email)
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .kakaoUserId(kakaoUserId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                userService.createUser(user);
            } else {
                // 기존 사용자 정보 업데이트
                user.setNickname(nickname);
                user.setProfileImage(profileImage);
                user.setKakaoUserId(kakaoUserId);
                user.setUpdatedAt(LocalDateTime.now());
                userService.updateUser(user);
            }

            // 5. JWT 토큰 생성
            String jwtToken = jwtTokenProvider.createToken(user.getId().toString());

            return LoginResponseDTO.builder()
                    .token(jwtToken)
                    .user(user)
                    .build();
        }
        catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생:", e);
            throw e;
        }}
}
