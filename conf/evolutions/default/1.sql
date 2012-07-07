# --- !Ups

CREATE TABLE league (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  league_name varchar(255),
  location varchar(255),
  description varchar(255),
  active boolean DEFAULT false
);

CREATE TABLE season (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  league_id bigint NOT NULL,
  start_date date NOT NULL,
  completed boolean DEFAULT FALSE,
  weeks_regular smallint DEFAULT 1,
  weeks_playoffs smallint DEFAULT 1,
  byes smallint DEFAULT 0,
  doubleheaders smallint DEFAULT 0,
  FOREIGN KEY (league_id) REFERENCES league(id) ON DELETE CASCADE
);

CREATE TABLE week (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  season_id bigint NOT NULL,
  game_date timestamp NOT NULL,
  playoff boolean,
  FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE
);

CREATE TABLE team (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  name varchar(255),
  captain_name varchar(255),
  captain_email varchar(200)
);

CREATE TABLE game (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  week_id bigint NOT NULL,
  start_time time NOT NULL,
  court smallint,
  team1_id bigint,
  team2_id bigint,
  num_sets smallint,
  FOREIGN KEY (week_id) REFERENCES week(id) ON DELETE CASCADE,
  FOREIGN KEY (team1_id) REFERENCES team(id),
  FOREIGN KEY (team2_id) REFERENCES team(id)
);

CREATE TABLE set (
  num smallint,
  game_id bigint,
  team1_score smallint,
  team2_score smallint,
  PRIMARY KEY (game_id, num),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE set;
DROP TABLE game;
DROP TABLE week;
DROP TABLE season;
DROP TABLE league;
DROP TABLE team;
