package com.savit.card.controller;

import com.savit.card.dto.CardRegisterRequest;
import com.savit.card.service.CardService;
import com.savit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerCardAndFetch(
            @RequestBody @Valid CardRegisterRequest req, HttpServletRequest request) {

        try {
            Long userId = jwtUtil.getUserIdFromToken(request);

            String connectedId =
                    cardService.registerAccount(req);

            List<Map<String,Object>> cards =
                    cardService.fetchCardList(
                            connectedId,
                            req.getOrganization(),
                            req.getBirthDate()
                    );

            cardService.saveCards(cards, connectedId, req.getOrganization(), userId, req.getCardPassword());

            return ResponseEntity.ok(Map.of(
                    "connectedId", connectedId,
                    "cards",       cards
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}