# 0006. React, Vite, TypeScript and react-chessboard, in the same repo

- Status: Accepted
- Date: 2026-11-22

## Context
We need a chessboard in a desktop browser for v1. Serhat knows some React and Angular, and wants to learn React properly.

## Options
- Build tool: **Vite** (fast, the current standard for single-page apps) vs. Next.js (adds server rendering and its
  own backend, which we don't need, since Spring is the backend).
- Language: **TypeScript** (types catch mistakes in API data early, and it feels familiar coming from Java) vs. JavaScript.
- Board: **react-chessboard** (draws the board, pieces and drag-and-drop) vs. building our own (good practice, but slow).
- Location: **`frontend/` in this repo** (one issue list, one PR for full-stack changes) vs. a separate repo.

## Decision
React + Vite + TypeScript with react-chessboard, in `frontend/`. During development, Vite proxies API calls to
Spring on port 8080.

## Consequences
- The board component only displays and reports moves. All rule checking stays on the server.
- The repo has two builds (Gradle and npm), so CI gets a frontend job.
- We can split the frontend into its own repo later if it becomes necessary.
