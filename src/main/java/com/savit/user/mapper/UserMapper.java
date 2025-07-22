package com.savit.user.mapper;


import com.savit.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    User findByKakaoUserId(String kakaoUserId);
    void insertUser(User user);
    void updateUser(User user);
}
