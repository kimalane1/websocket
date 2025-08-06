package com.yolo.game.demo.scheduler;

import com.yolo.game.demo.service.GameService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

@Component
public class StartupRunner implements CommandLineRunner {
    private final GameService gameService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final boolean ENABLED;
    private final int DELAY;

    public StartupRunner(GameService gameService,
                         @Value("${game.enabled:true}") boolean ENABLED,
                         @Value("${game.delay:10}") int DELAY) {
        this.gameService = gameService;
        this.ENABLED = ENABLED;
        this.DELAY = DELAY;
    }

    @Override
    public void run(String... args) {
        if (!ENABLED) return;
        startRoundCycle();
    }

    private void startRoundCycle() {
        scheduler.scheduleAtFixedRate(() -> {
            gameService.startNewRound();
            int winningNumber = RandomGenerator.getDefault().nextInt(1, 11);
            scheduler.schedule(() -> gameService.finishRound(winningNumber), DELAY, TimeUnit.SECONDS);
        }, 0, 15, TimeUnit.SECONDS);
    }
}
