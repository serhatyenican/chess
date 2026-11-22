# 0009. Single Gradle module, engine package without Spring

- Status: Accepted
- Date: 2026-11-22

## Context
The chess engine (ADR 0004) should be pure Java, easy to unit-test and independent of web code. We also want a
simple build while learning.

## Options
1. **Multi-module Gradle build** (`engine`, `app`): the compiler enforces the boundary, but more setup up front.
2. **One module with packages by responsibility**: simplest, but the rule is only a convention.
3. **One module plus an architecture test** (ArchUnit): a simple build, with the rule enforced by a failing test.

## Decision
One module. Engine code lives in `org.srt.chess.engine` and must not import Spring, JPA or web classes. An ArchUnit
test for this rule is added in v1 · M2.

## Consequences
- If the engine grows or gets reused, it can move to its own module later with little effort, because it has no
  dependencies.
