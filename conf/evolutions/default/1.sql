# Tasks schema aa

# --- !Ups

CREATE TABLE league (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  league_name varchar(255),
  location varchar(255),
  description varchar(255),
  active boolean DEFAULT 0
);

CREATE TABLE season (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  league_id bigint NOT NULL,
  start_date date NOT NULL,
  completed boolean DEFAULT FALSE,
  weeks_regular tinyint DEFAULT 1,
  weeks_playoffs tinyint DEFAULT 1,
  byes tinyint DEFAULT 0,
  doubleheaders tinyint DEFAULT 0,
  FOREIGN KEY (league_id) REFERENCES league(id) ON DELETE CASCADE
);

CREATE TABLE week (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  season_id bigint NOT NULL,
  game_date datetime NOT NULL,
  playoff boolean,
  FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE
);

CREATE TABLE team (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  name varchar(255),
  captain_name varchar(255),
  captain_email varchar(200)
);

CREATE TABLE game (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  week_id bigint NOT NULL,
  start_time time NOT NULL,
  court tinyint,
  team1_id bigint,
  team2_id bigint,
  num_sets tinyint,
  FOREIGN KEY (week_id) REFERENCES week(id) ON DELETE CASCADE,
  FOREIGN KEY (team1_id) REFERENCES team(id),
  FOREIGN KEY (team2_id) REFERENCES team(id)

);

CREATE TABLE set (
  num tinyint,
  game_id bigint,
  team1_score tinyint,
  team2_score tinyint,

  PRIMARY KEY (game_id, num),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE league;
DROP TABLE season;
DROP TABLE week;
DROP TABLE team;
DROP TABLE game;
DROP TABLE set;
