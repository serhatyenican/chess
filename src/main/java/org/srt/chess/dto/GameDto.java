package org.srt.chess.dto;

import org.srt.chess.entity.Game;

public record GameDto(String id) {
    public GameDto(Game game) {
        this(game.getId());
    }
}
