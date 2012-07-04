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

case class Week(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean)

case class GameWeekWithGames(week: Week, games: Seq[Option[Game]])

object Week {
  val simpleParser = {
    get[Pk[Long]]("week.id") ~
      long("week.season_id") ~
      date("week.game_date") ~
      bool("week.playoff") map {
      case id ~ season_id ~ game_date ~ playoff => new Week(id, season_id, new DateTime(game_date), playoff)
    }
  }

  def findByIdWithGames(weekId: Long): Option[GameWeekWithGames] = DB.withConnection {
    implicit c =>
      val weekAndMatches: List[(Week, Option[Game])] = SQL(
        """
          SELECT * FROM week
          LEFT OUTER JOIN game ON game.week_id = week.id
          WHERE week.id = {id}
        """
      )
        .on('id -> weekId)
        .as((Week.simpleParser ~ (Game.simpleParser ?)) *) map (flatten)

      weekAndMatches.headOption.map {
        f => GameWeekWithGames(f._1, weekAndMatches.map(_._2))
      }
  }

  def create(seasonId: Long, gameTime: DateTime, playoff: Boolean = false): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into week (season_id, game_date, playoff) " +
        "values ({season_id}, {game_date}, {playoff})").
        on('season_id -> seasonId, 'game_date -> gameTime.toDate, 'playoff -> playoff)
        .executeInsert()
  }

}
