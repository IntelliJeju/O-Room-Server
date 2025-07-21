package com.oroom.user.service;

import com.oroom.security.KakaoProperties;
import com.oroom.user.dto.KakaoTokenDTO;
import com.oroom.user.dto.KakaoUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthServiceImpl implements KakaoOAuthService {

    private final RestTemplate restTemplate;
    private final KakaoProperties kakaoProperties;

    @Override
    public KakaoTokenDTO getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());
        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenDTO> response = restTemplate.postForEntity(
                    kakaoProperties.getTokenUrl(), request, KakaoTokenDTO.class);

            if (response.getBody() == null) {
                throw new RuntimeException("카카오 토큰 응답이 null입니다.");
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 토큰 요청 실패. code: {}, error: {}", code, e.getMessage());
            throw new RuntimeException("카카오 토큰 요청 실패", e);
        }
    }

    @Override
    public KakaoUserDTO getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("property_keys", "[\"kakao_account.email\", \"kakao_account.profile\", \"properties.nickname\", \"properties.profile_image\"]");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoUserDTO> response = restTemplate.postForEntity(
                    kakaoProperties.getUserInfoUrl(), request, KakaoUserDTO.class);

            if (response.getBody() == null) {
                throw new RuntimeException("카카오 사용자 정보 응답이 null입니다.");
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패. error: {}", e.getMessage());
            throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
        }
    }
}