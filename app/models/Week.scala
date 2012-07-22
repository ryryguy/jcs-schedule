package models

import org.joda.time.DateTime
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import scala._
import anorm.~
import scala.Some
import scala.Boolean
import scala.Long
import views.html.week
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 */

abstract class Week() {
  def id: Pk[Long]
  def playoff: Boolean
  def gameDate: DateTime
}

case class WeekUnscheduled(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean) extends Week

case class WeekScheduled(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean, games: Seq[ScheduledGame]) extends Week

case class WeekCompleted(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean, games: Seq[CompletedGame]) extends Week

object Week {

  implicit object WeekOrdering extends Ordering[Week] {
    def compare(x: Week, y: Week) = x.gameDate.compareTo(y.gameDate)
  }

  val weekRowParser = {
    get[Pk[Long]]("week.id") ~ long("week.season_id") ~ date("week.game_date") ~ bool("week.playoff")
  }

  val weekWithGamesParser = {
    (weekRowParser ~ (Game.gamesAndSetsParser ?)) map {
      case id ~ season_id ~ game_date ~ playoff ~ Some(game: ScheduledGame) => WeekScheduled(id, season_id, new DateTime(game_date), playoff, List(game))
      case id ~ season_id ~ game_date ~ playoff ~ Some(game: CompletedGame) => WeekCompleted(id, season_id, new DateTime(game_date), playoff, List(game))
      case id ~ season_id ~ game_date ~ playoff ~ _ => WeekUnscheduled(id, season_id, new DateTime(game_date), playoff)
    }
  }
  def processWeeks(rows: List[Week]): List[Week] =
    rows match {
      case Nil => Nil
      case (weekScheduled: WeekScheduled) :: tail => {
        val (thisWeekRows, remainingRows) = rows.span(_.id == weekScheduled.id)
        weekScheduled.copy(games = Game.processGames(thisWeekRows.asInstanceOf[List[WeekScheduled]].flatMap(_.games)).asInstanceOf[List[ScheduledGame]]) :: processWeeks(remainingRows)
      }
      case (weekCompleted: WeekCompleted) :: tail => {
        val (thisWeekRows, remainingRows) = rows.span(_.id == weekCompleted.id)
        // asInstanceOf used below to avoid type erasure warning
        // bad data might mix in WeekScheduled here, so filter for safety
        weekCompleted.copy(games = Game.processGames(thisWeekRows.filter(_.isInstanceOf[WeekCompleted]).asInstanceOf[List[WeekCompleted]].flatMap(_.games)).asInstanceOf[List[CompletedGame]]) :: processWeeks(remainingRows)
      }
      case (weekUnscheduled: WeekUnscheduled) :: tail => (weekUnscheduled :: processWeeks(tail))
    }

  def findBySeasonId(seasonId: Long): List[Week] = DB.withConnection {
    implicit c =>
      processWeeks(
        (SQL(
          """
    SELECT * FROM week
    LEFT OUTER JOIN game ON game.week_id = week.id
    LEFT OUTER JOIN set ON set.game_id = game.id
    WHERE week.season_id = {season_id}
    ORDER BY week.id, game.id, set.num
          """
        )
          .on('season_id -> seasonId)
          .as(weekWithGamesParser *))
      ).sorted
  }

  def findByTeamId(seasonId: Long, teamId: Long): List[Week] = DB.withConnection {
    implicit c =>
      processWeeks(
        (SQL(
          """
    SELECT * FROM week
    LEFT OUTER JOIN game ON game.week_id = week.id
    LEFT OUTER JOIN set ON set.game_id = game.id
    WHERE week.season_id = {season_id}
    AND (game.team1_id = {team_id} OR game.team2_id = {team_id})
    ORDER BY week.id, game.id, set.num
          """
        )
          .on('season_id -> seasonId, 'team_id -> teamId)
          .as(weekWithGamesParser *))
      ).sorted
  }

  def create(seasonId: Long, gameDate: DateTime, playoff: Boolean = false): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into week (season_id, game_date, playoff) " +
        "values ({season_id}, {game_date}, {playoff})").
        on('season_id -> seasonId, 'game_date -> gameDate.toDate, 'playoff -> playoff)
        .executeInsert()
  }
}
