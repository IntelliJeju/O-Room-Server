package com.savit.notification.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.savit.notification.domain.PushNotification;
import com.savit.notification.domain.UserFcmToken;

import java.util.List;

@Mapper
public interface NotificationMapper {

    void insertNotification(PushNotification notification);

    void insertUserFcmToken(@Param("userId") Long userId,
                            @Param("fcmToken") String fcmToken,
                            @Param("deviceType") String deviceType);

    void deactivateUserTokens(@Param("userId") Long userId,
                              @Param("deviceType") String deviceType);

    List<UserFcmToken> findActiveTokensByUserId(@Param("userId") Long userId);

    UserFcmToken findTokenByUserIdAndDeviceType(@Param("userId") Long userId,
                                                @Param("deviceType") String deviceType);
}