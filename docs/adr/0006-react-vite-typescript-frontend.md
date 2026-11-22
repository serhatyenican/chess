# 0006. React with Vite and TypeScript for the frontend

- Status: Accepted
- Date: 2026-11-22

## Context
We need a browser chessboard that works on desktop and mobile. Serhat knows some React and Angular, and wants to
learn React properly.

## Options
- Framework: **React** (learning goal, huge ecosystem, chessboard components available) vs. Angular (already known)
  vs. plain HTML/JS or server-rendered Thymeleaf (less to learn, but a poor fit for a live board).
- Tooling: **Vite** (fast dev server, the current standard for single-page apps) vs. Next.js (adds server rendering,
  which we don't need, because Spring is the backend).
- Language: **TypeScript** (types for game messages and API responses catch mistakes early) vs. JavaScript.

## Decision
React + Vite + TypeScript, in a `frontend/` folder of this repo.

## Consequences
- One repo, two builds (Gradle and npm). CI gets a second job when the frontend arrives.
- Deployment options: serve the built files from Spring Boot (simplest), or host them separately. That will be
  decided in M6.
