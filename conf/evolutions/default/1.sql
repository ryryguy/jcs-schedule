# Tasks schema

# --- !Ups

CREATE TABLE league (
    id integer AUTO_INCREMENT PRIMARY KEY,
    league_name varchar(255),
    location varchar(255),
    description varchar(255),
    active boolean DEFAULT 0
);

# --- !Downs

DROP TABLE league;
