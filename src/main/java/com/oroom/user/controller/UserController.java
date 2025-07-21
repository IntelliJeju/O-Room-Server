package com.oroom.user.controller;

import com.oroom.user.domain.User;
import com.oroom.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(HttpServletRequest request) {
        // JWT 토큰에서 사용자 ID 추출하여 사용자 정보 조회
        // 실제 구현에서는 JWT 필터에서 사용자 정보를 추출해야 함
        return ResponseEntity.ok().build();
    }
}