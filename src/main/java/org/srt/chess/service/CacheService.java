package org.srt.chess.service;

import org.springframework.stereotype.Service;
import org.srt.chess.entity.Game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CacheService {
    Map<String, Game> games = new HashMap<>();
    public void save(Game game) {
        games.put(game.getId(), game);
    }

    public Game getGame(String id) {
        return games.get(id);
    }

    public List<Game> getGames() {
        return games.values().stream().toList();
    }
}
