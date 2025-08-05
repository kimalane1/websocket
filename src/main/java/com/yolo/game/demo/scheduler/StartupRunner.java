package com.yolo.game.demo.scheduler;

import com.yolo.game.demo.service.GameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    private final GameService gameService;

    public StartupRunner(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void run(String... args) {
        gameService.startNewRound();
    }
}
