package com.yolo.game.demo.unit.service;

import com.yolo.game.demo.model.BetRequest;
import com.yolo.game.demo.model.BetResponse;
import com.yolo.game.demo.service.GameService;
import com.yolo.game.demo.service.NotificationService;
import com.yolo.game.demo.session.PlayerSessionStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameServiceRtpTest {
    private final PlayerSessionStore playerSessionStore = new PlayerSessionStore();
    private final NotificationService notificationService = new NotificationService(playerSessionStore);
    private final GameService gameService = new GameService(notificationService, 10);

    @Test
    void shouldCalculateRtpForMillionRounds() throws Exception {
        int totalRounds = 1_000_000;
        int numberOfThreads = 24;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(totalRounds);

        AtomicReference<BigDecimal> spent = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> won = new AtomicReference<>(BigDecimal.ZERO);

        for (int i = 0; i < totalRounds; i++) {
            executor.submit(() -> {
                try {
                    BetRequest bet = new BetRequest("Erik", 5, BigDecimal.ONE);

                    int winningNumber = ThreadLocalRandom.current().nextInt(1, 11);
                    BetResponse response = gameService.evaluate(winningNumber, bet);

                    spent.updateAndGet(v -> v.add(BigDecimal.ONE));
                    won.updateAndGet(v -> v.add(response.amount()));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        BigDecimal spentTotal = spent.get();
        BigDecimal wonTotal = won.get();
        BigDecimal rtp = wonTotal.divide(spentTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        System.out.println("Total spent: " + spentTotal);
        System.out.println("Total won: " + wonTotal);
        System.out.println("RTP: " + rtp + "%");

        assertTrue(rtp.compareTo(BigDecimal.valueOf(90)) > 0);
    }

}
