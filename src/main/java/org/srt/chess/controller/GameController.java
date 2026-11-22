package org.srt.chess.controller;

import org.springframework.web.bind.annotation.*;
import org.srt.chess.dto.GameDto;
import org.srt.chess.entity.Game;
import org.srt.chess.service.GameService;

import java.util.List;

@RestController
@RequestMapping("game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public GameDto createGame() {
        return gameService.create();
    }

    @GetMapping("/{id}")
    public Game getGame(@PathVariable String id) {
        return gameService.get(id);
    }

    @GetMapping()
    public List<Game> getGames() {
        return gameService.getGames();
    }
}
