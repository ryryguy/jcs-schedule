# Tasks schema

# --- !Ups

CREATE TABLE league (
    id integer PRIMARY KEY,
    league_name varchar(255),
    location varchar(255),
    description varchar(255),
    active tinyInt DEFAULT 0
);

# --- !Downs

DROP TABLE league;
