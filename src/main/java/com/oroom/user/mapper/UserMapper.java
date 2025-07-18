package com.oroom.user.mapper;


import com.oroom.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    User findByKakaoUserId(String kakaoUserId);
    void insertUser(User user);
    void updateUser(User user);
}
