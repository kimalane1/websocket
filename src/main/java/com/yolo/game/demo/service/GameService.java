package com.yolo.game.demo.service;

import com.yolo.game.demo.model.BetRequest;
import com.yolo.game.demo.model.BetResponse;
import com.yolo.game.demo.model.RoundState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import static com.yolo.game.demo.model.RoundState.*;

@Service
public class GameService {
    public static final BigDecimal RATE = new BigDecimal("9.9");
    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private final Map<String, BetRequest> currentBets = new ConcurrentHashMap<>();

    private final NotificationService notificationService;
    private volatile RoundState roundState = FINISHED;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    Map<String, BigDecimal> winners = new HashMap<>();

    public GameService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void startNewRound() {
        if (roundState != FINISHED) return;

        roundState = WAITING_FOR_BETS;
        log.info("New round started: accepting bets for 10 seconds");

        scheduler.schedule(this::finishRound, 10, TimeUnit.SECONDS);
    }

    private void finishRound() {
        roundState = CALCULATING_RESULTS;
        log.info("Round ended: calculating results");

        var winningNumber = RandomGenerator.getDefault().nextInt(1, 11);

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

        scheduler.schedule(this::startNewRound, 5, TimeUnit.SECONDS);
    }

    public void receive(BetRequest request) {
        if (roundState != WAITING_FOR_BETS) {
            log.info("Bet rejected from {}, currently not accepting bets", request.nick());
            return;
        }
        currentBets.put(request.nick(), request);
        log.info("Bet accepted from {}", request.nick());
    }

    public BetResponse evaluate(int winningNumber, BetRequest request) {
        var success = false;
        var amount = BigDecimal.ZERO;

        if (winningNumber == request.bet()) success = true;
        if (success) amount = request.amount().multiply(RATE).setScale(2, RoundingMode.HALF_UP);

        return new BetResponse(request.nick(), success, request.bet(), winningNumber, amount);
    }

    public Map<String, BetRequest> getCurrentBets() {
        return currentBets;
    }
}
