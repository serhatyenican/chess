/**
 * The chess engine: board, pieces, moves and the rules of chess, in pure Java.
 * <p>
 * The engine knows nothing about HTTP, Spring or the database. It must not import Spring, JPA or Hibernate
 * classes, so its tests stay fast plain JUnit and the v3 bot can reuse it. An ArchUnit test enforces this
 * (ADR 0009).
 */
package org.srt.chess.engine;
