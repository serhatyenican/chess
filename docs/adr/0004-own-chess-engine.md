# 0004. Write our own chess engine

- Status: Accepted
- Date: 2026-11-22

## Context
The server is the authority on chess rules and validates every move: castling, en passant, promotion, check,
checkmate and stalemate.

## Options
1. **Use a library** (e.g. chesslib): working in a day, battle-tested, but little learning.
2. **Write our own engine**: about one milestone of work, and excellent practice in object-oriented design and
   test-driven development.

## Decision
Write our own engine in milestone v1 · M2, in pure Java, developed test-first.

## Consequences
- Rule bugs are ours to find, so the engine needs thorough unit tests, including known tricky positions.
- Positions are read and written as FEN, the standard notation, which also makes test cases easy to write.
