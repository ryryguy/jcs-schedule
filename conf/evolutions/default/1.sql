# Tasks schema aa

# --- !Ups

CREATE TABLE league (
  id IDENTITY,
  league_name varchar(255),
  location varchar(255),
  description varchar(255),
  active boolean DEFAULT 0
);

CREATE TABLE season (
  id IDENTITY,
  league_id bigint NOT NULL,
  start_date date NOT NULL,
  completed boolean DEFAULT FALSE,
  weeks_regular tinyint DEFAULT 1,
  weeks_playoffs tinyint DEFAULT 1,
  byes tinyint DEFAULT 0,
  doubleheaders tinyint DEFAULT 0,
  FOREIGN KEY (league_id) REFERENCES league(id) ON DELETE CASCADE
);

CREATE TABLE gamenight (
  id IDENTITY,
  season_id bigint NOT NULL,
  game_date datetime NOT NULL,
  playoff boolean,
  FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE
);

CREATE TABLE team (
  id IDENTITY,
  name varchar(255),
  captain_name varchar(255),
  captain_email varchar(200)
);

CREATE TABLE match (
  id IDENTITY,
  game_night_id bigint NOT NULL,
  start_time time NOT NULL,
  court tinyint,
  team1_id bigint,
  team2_id bigint,
  num_sets tinyint,
  status varchar(10),   -- FUTURE, COMPLETED, CANCELLED
  FOREIGN KEY (game_night_id) REFERENCES gamenight(id) ON DELETE CASCADE,
  FOREIGN KEY (team1_id) REFERENCES team(id),
  FOREIGN KEY (team2_id) REFERENCES team(id)

);

CREATE TABLE set (
  num tinyint,
  match_id bigint,
  team1_score tinyint,
  team2_score tinyint,

  PRIMARY KEY (match_id, num),
  FOREIGN KEY (match_id) REFERENCES match(id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE league;
DROP TABLE season;
DROP TABLE gamenight;
DROP TABLE team;
DROP TABLE match;
DROP TABLE set;
