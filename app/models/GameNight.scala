package models

import org.joda.time.{LocalTime, LocalDate, DateTime, DateMidnight}
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */

case class GameNight(id: Long, seasonId: Long, gameDate: DateTime, playoff: Boolean)

object GameNight {
  val season = {
    long("id") ~
      long("season_id") ~
      date("game_date") ~
      bool("playoff") map {
      case id ~ season_id ~ game_date ~ playoff => new GameNight(id, season_id, new DateTime(game_date), playoff)
    }
  }

  def create(seasonId: Long, gameTime: DateTime, playoff: Boolean = false): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into gamenight (season_id, game_date, playoff) " +
        "values ({season_id}, {game_date}, {playoff})").
        on('season_id -> seasonId, 'game_date -> gameTime.toDate, 'playoff -> playoff)
        .executeInsert()
  }
}
