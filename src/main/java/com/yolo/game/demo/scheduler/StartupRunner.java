package com.yolo.game.demo.scheduler;

import com.yolo.game.demo.service.GameService;
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

    public StartupRunner(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void run(String... args) {
        startRoundCycle();
    }

    private void startRoundCycle() {
        scheduler.scheduleAtFixedRate(() -> {
            gameService.startNewRound();
            int winningNumber = RandomGenerator.getDefault().nextInt(1, 11);
            scheduler.schedule(() -> gameService.finishRound(winningNumber), 10, TimeUnit.SECONDS);
        }, 0, 15, TimeUnit.SECONDS);
    }
}
