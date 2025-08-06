package com.yolo.game.demo.service;

import com.yolo.game.demo.model.BetRequest;
import com.yolo.game.demo.model.BetResponse;
import com.yolo.game.demo.model.RoundState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

import static com.yolo.game.demo.model.RoundState.*;
@Slf4j
@Service
public class GameService {
    public static final BigDecimal RATE = new BigDecimal("9.9");
    @Getter
    private final Map<String, BetRequest> currentBets = new ConcurrentHashMap<>();

    private final NotificationService notificationService;
    private volatile RoundState roundState = FINISHED;


    Map<String, BigDecimal> winners = new HashMap<>();

    public GameService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void startNewRound() {
        if (roundState != FINISHED) return;

        roundState = WAITING_FOR_BETS;
        log.info("New round started: accepting bets for 10 seconds");
    }

//    public void finishRound() {
//
//        finishRound(winningNumber);
//    }

    public void finishRound(int winningNumber) {
        roundState = CALCULATING_RESULTS;
        log.info("Round ended: calculating results");


        for (Map.Entry<String, BetRequest> entry : currentBets.entrySet()) {
            BetResponse response = evaluate(winningNumber, entry.getValue());

            if (response.success()) winners.merge(response.nick(), response.amount(), BigDecimal::add);
            try {
                notificationService.sendToPlayer(response.nick(), response.toMessage());
            } catch (Exception e) {
                log.warn("Failed to notify player {}: {}", response.nick(), e.getMessage());
            }
        }
        try {
            notificationService.broadcast(winners);
        } catch (Exception e) {
            log.error("Broadcast failed", e);
        }

        currentBets.clear();
        roundState = FINISHED;
    }

    public void receive(BetRequest request) {
        if (roundState != WAITING_FOR_BETS) {
            notificationService.sendToPlayer(request.nick(), String.format("Bet rejected: currently not accepting bets from %s", request.nick()));
            log.info("Bet rejected from {}, currently not accepting bets", request.nick());
            return;
        }
        currentBets.put(request.nick(), request);
        log.info("Bet accepted from {}", request.nick());
        notificationService.sendToPlayer(request.nick(), String.format("Bet accepted from %s", request.nick()));
    }

    public BetResponse evaluate(int winningNumber, BetRequest request) {
        var success = false;
        var amount = BigDecimal.ZERO;

        if (winningNumber == request.bet()) success = true;
        if (success) amount = request.amount().multiply(RATE).setScale(2, RoundingMode.HALF_UP);

        return new BetResponse(request.nick(), success, request.bet(), winningNumber, amount);
    }

}
