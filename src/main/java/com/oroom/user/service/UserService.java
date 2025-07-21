package com.oroom.user.service;

import com.oroom.user.domain.User;

public interface UserService {
    User findByEmail(String email);
    User findByKakaoUserId(String kakaoUserId);
    void createUser(User user);
    void updateUser(User user);
}