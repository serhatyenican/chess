package org.srt.chess.service;

import org.springframework.stereotype.Service;
import org.srt.chess.dto.GameDto;
import org.srt.chess.entity.Game;
import org.srt.chess.exception.GameNotFoundException;
import org.srt.chess.repository.GameRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final CacheService cacheService;
    private final GameRepository gameRepository;

    public GameService(CacheService cacheService, GameRepository gameRepository) {
        this.cacheService = cacheService;
        this.gameRepository = gameRepository;
    }

    public GameDto create() {
        var game = new Game();
        cacheService.save(game);
        gameRepository.save(game);
        return new GameDto(game);
    }

    public Game get(String id) {
        return Optional.ofNullable(cacheService.getGame(id)).orElseThrow(() -> new GameNotFoundException(id));
    }

    public List<Game> getGames() {
        return cacheService.getGames();
    }
}
