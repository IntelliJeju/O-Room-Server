package com.savit.card.repository;

import com.savit.card.domain.CodefToken;
import com.savit.card.mapper.CodefTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CodefTokenRepository {
    private final CodefTokenMapper mapper;

    public Optional<CodefToken> findValidToken() {
        return Optional.ofNullable(mapper.findValidToken());
    }
    public void save(CodefToken token) {
        if (mapper.exists()) {
            mapper.updateToken(token);
        } else {
            mapper.insertToken(token);
        }
    }
}