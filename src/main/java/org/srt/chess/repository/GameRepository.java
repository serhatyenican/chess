package org.srt.chess.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.srt.chess.entity.Game;

public interface GameRepository extends JpaRepository<Game, String> {
}
