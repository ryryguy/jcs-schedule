# Tasks schema aa

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

CREATE TABLE gamenight (
    id integer IDENTITY,
    season_id integer NOT NULL,
    game_date datetime NOT NULL,
    FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE
);

CREATE TABLE team (
    id integer IDENTITY,
    name varchar(255),
    captain_name varchar(255),
    captain_email varchar(200)
);

CREATE TABLE match (
    id integer IDENTITY,
    game_night_id integer NOT NULL,
    start_time time NOT NULL,
    court integer,
    team1_id integer,
    team2_id integer,
    num_sets tinyint,
    status varchar(10),   -- FUTURE, COMPLETED, CANCELLED
    FOREIGN KEY (game_night_id) REFERENCES gamenight(id) ON DELETE CASCADE,
    FOREIGN KEY (team1_id) REFERENCES team(id),
    FOREIGN KEY (team2_id) REFERENCES team(id)
    -- playoff?
);

CREATE TABLE set (
    id integer,
    match_id integer,
    team1_score tinyint,
    team2_score tinyint,

    PRIMARY KEY (id, match_id),
    FOREIGN KEY (match_id) REFERENCES match(id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE league;
DROP TABLE season;
DROP TABLE gamenight;
DROP TABLE team;
DROP TABLE match;
DROP TABLE set;
