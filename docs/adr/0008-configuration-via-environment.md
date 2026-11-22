# 0008. Configuration and secrets via environment variables

- Status: Accepted
- Date: 2026-11-22

## Context
The first prototype had the database password in `application.yaml` and `docker-compose.yml`, which would have
published it on GitHub.

## Options
1. **Values in committed config files**: easy, but leaks secrets and needs different files per environment.
2. **Environment variables** (twelve-factor app): the same build runs anywhere, and only the environment changes.
3. **A secrets manager** (Vault, a cloud secret store): best for production, overkill locally.

## Decision
Environment variables. Locally they live in a git-ignored `.env` file, which docker-compose reads automatically, and
which Spring imports via `spring.config.import: optional:file:.env[.properties]`. `.env.example` documents the
variable names with placeholder values.

## Consequences
- Never commit `.env` or real secrets. Reviews check for it.
- CI and any later hosting set the same variable names.
