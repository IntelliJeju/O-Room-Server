package com.savit.card.mapper;

import com.savit.card.domain.CodefToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodefTokenMapper {
    CodefToken findValidToken();
    boolean exists();
    int insertToken(CodefToken token);
    int updateToken(CodefToken token);
}
