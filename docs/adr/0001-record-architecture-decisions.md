# 0001. Record architecture decisions

- Status: Accepted
- Date: 2026-11-22

## Context
This is a learning project, and understanding *why* is as important as the code. Decisions also need to survive
between sessions: future work (including AI coding sessions in the IDE, which only see the repo) should not re-argue
settled choices or silently drift from them.

## Options
1. **Decisions only in chat or issues**: no effort, but scattered and hard to find later.
2. **A long design document**: one place, but it goes stale and nobody knows which parts still hold.
3. **Architecture Decision Records** (one short, immutable file per decision): small, dated, versioned with the code.

## Decision
Use ADRs in `docs/adr/`, following Michael Nygard's format (context, options, decision, consequences).

## Consequences
- Every significant technical choice gets a half-page ADR in the same PR that introduces it.
- `CLAUDE.md` points here, so AI sessions read the decisions before changing things.
