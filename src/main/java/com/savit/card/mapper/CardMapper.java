package com.savit.card.mapper;

import com.savit.card.domain.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardMapper {
    void insertCards(@Param("cards") List<Card> cards);
}
