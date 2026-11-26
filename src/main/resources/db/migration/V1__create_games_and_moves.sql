-- Games and their moves (ADR 0005, ADR 0011).

create table games
(
    id         uuid        primary key,
    status     varchar     not null,
    winner     varchar,
    fen        varchar     not null,
    version    bigint      not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint games_status_known check (status in ('IN_PROGRESS', 'CHECKMATE', 'STALEMATE', 'LEFT')),
    constraint games_winner_known check (winner in ('WHITE', 'BLACK')),
    constraint games_winner_only_on_checkmate check ((winner is not null) = (status = 'CHECKMATE'))
);

create table moves
(
    id        bigint generated always as identity primary key,
    game_id   uuid        not null references games (id),
    ply       int         not null,
    move      varchar     not null,
    played_at timestamptz not null,
    constraint moves_ply_positive check (ply > 0),
    constraint moves_one_per_ply unique (game_id, ply)
);
