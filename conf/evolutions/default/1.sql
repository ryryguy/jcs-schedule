# Tasks schema

# --- !Ups

CREATE TABLE league (
    id integer IDENTITY,
    league_name varchar(255),
    location varchar(255),
    description varchar(255),
    active boolean DEFAULT 0
);

CREATE TABLE season (
    id integer IDENTITY,
    league_id integer NOT NULL,
    start_date date NOT NULL,
    completed boolean DEFAULT FALSE,
    weeks_regular integer DEFAULT 1,
    weeks_playoffs integer DEFAULT 1,
    byes integer DEFAULT 0,
    doubleheaders integer DEFAULT 0,
    FOREIGN KEY (league_id) REFERENCES league(id) ON DELETE CASCADE
);

CREATE TABLE game (
    id integer IDENTITY,
    season_id integer NOT NULL,
    game_date date NOT NULL,
    start_time time NOT NULL,
    FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE
);

CREATE TABLE match (
    id integer IDENTITY,
    game_id integer NOT NULL,
    start_time time NOT NULL,
    court integer,
    team1 integer,
    team2 integer,
    team1_score tinyint,
    team2_score tinyint,
    status varchar(10),   -- FUTURE, COMPLETED, CANCELLED
    FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
    -- playoff?
);

# --- !Downs

DROP TABLE league;
DROP TABLE season;
DROP TABLE game;
DROP TABLE match;
