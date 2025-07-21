package com.oroom.user.controller;

import com.oroom.security.KakaoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final KakaoProperties kakaoProperties;

    @GetMapping("/")
    public String home() {
        // 서버 시작하자마자 바로 카카오 로그인으로 리다이렉트
        return "redirect:/auth/kakao";
    }

    @GetMapping("/login")
    public String login() {
        // /login 접속 시에도 카카오 로그인으로 리다이렉트
        return "redirect:/auth/kakao";
    }
}