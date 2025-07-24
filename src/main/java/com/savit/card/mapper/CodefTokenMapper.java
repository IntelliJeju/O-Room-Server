package com.savit.card.mapper;

import com.savit.card.domain.CodefToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodefTokenMapper {
    CodefToken findValidToken();
    int insertToken(CodefToken token);
}
