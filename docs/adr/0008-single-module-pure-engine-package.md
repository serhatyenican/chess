# 0008. Single Gradle module, engine package without Spring

- Status: Accepted
- Date: 2026-11-22

## Context
The chess rules engine should be pure Java, easy to unit-test and independent of web code. We also want a simple
build while learning.

## Options
1. **Multi-module Gradle build** (`engine`, `app`): the compiler enforces that the engine can't use Spring. But it
   means more build configuration up front.
2. **One module, with packages by responsibility**: simplest build, but the rule is only a convention.
3. **One module plus an architecture test** (ArchUnit): simple build, and a failing test enforces the rule.

## Decision
One module. Engine code lives in `org.srt.chess.engine` and must not import Spring, JPA or web classes. We add an
ArchUnit test for this rule in M2, when the engine package appears.

## Consequences
- If the engine grows or gets reused, it can be split into its own module later with little effort, because it
  has no dependencies.
