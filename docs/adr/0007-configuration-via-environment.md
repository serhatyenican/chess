# 0007. Configuration and secrets via environment variables

- Status: Accepted
- Date: 2026-11-22

## Context
The first prototype had the database password in `application.yaml` and `docker-compose.yml`, which would have
published it on GitHub.

## Options
1. **Values in committed config files**: easy, but it leaks secrets and needs different files per environment.
2. **Environment variables** (twelve-factor app approach): the same build runs anywhere, and only the environment changes.
3. **A secrets manager** (Vault, a cloud secret store): best for production, but overkill locally.

## Decision
Environment variables. Locally they live in a git-ignored `.env` file, which docker-compose reads automatically and
Spring imports via `spring.config.import: optional:file:.env[.properties]`. `.env.example` documents the variable
names with placeholder values.

## Consequences
- Never commit `.env` or real secrets. Reviews check for it.
- CI and hosting set the same variable names. A cloud secrets manager can feed them later.
