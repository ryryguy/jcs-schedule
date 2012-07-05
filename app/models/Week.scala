package models

import org.joda.time.{LocalTime, LocalDate, DateTime, DateMidnight}
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current
import scala._
import anorm.~
import scala.Some
import scala.Boolean
import scala.Long

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */

case class Week(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean)
case class WeekWithGames(week: Week, games: Seq[Option[Game]])

object Week {
  implicit object WeekOrdering extends Ordering[Week] {
    def compare(x: Week, y: Week) = x.gameDate.compareTo(y.gameDate)
  }

  implicit object WeekWithGamesOrdering extends Ordering[WeekWithGames] {
    def compare(x: WeekWithGames, y: WeekWithGames) = x.week.gameDate.compareTo(y.week.gameDate)
  }

  val simpleParser = {
    get[Pk[Long]]("week.id") ~
      long("week.season_id") ~
      date("week.game_date") ~
      bool("week.playoff") map {
      case id ~ season_id ~ game_date ~ playoff => new Week(id, season_id, new DateTime(game_date), playoff)
    }
  }

  // Map[Week, Option[List[Game]]]
  def findByIdWithGames(weekId: Long) : Map[Week, Option[List[Game]]] = DB.withConnection {
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

      weekAndMatches.head match {
        case (w, go) if go == None => Map(w -> None)
        case (w, _) => weekAndMatches.groupBy(_._1).mapValues(l => Some(l map(_._2.get)))
      }
  }

  def allSeasonWithGames(seasonId: Long) : List[WeekWithGames] = DB.withConnection {
    implicit c =>
      val rs : List[(Week, Option[Game])] =
        SQL(
        """
          SELECT * FROM week
          LEFT OUTER JOIN game ON game.week_id = week.id
          WHERE week.season_id = {season_id}
        """
      )
        .on('season_id -> seasonId)
        .as((Week.simpleParser ~ (Game.simpleParser ?)) *) map (flatten)

      (for ((week, gamelist) <- rs.groupBy(_._1).mapValues(l => l map (_._2))) yield {
        WeekWithGames(week, gamelist)
      }).toList
  }

  def create(seasonId: Long, gameTime: DateTime, playoff: Boolean = false): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into week (season_id, game_date, playoff) " +
        "values ({season_id}, {game_date}, {playoff})").
        on('season_id -> seasonId, 'game_date -> gameTime.toDate, 'playoff -> playoff)
        .executeInsert()
  }

}
