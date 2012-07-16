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

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 */

abstract class Week() {
  def id: Pk[Long]

  def gameDate: DateTime
}

case class WeekUnscheduled(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean) extends Week

case class WeekScheduled(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean, games: Seq[ScheduledGame]) extends Week {
  def this(week: WeekUnscheduled, games: Seq[ScheduledGame]) = this(week.id, week.seasonId, week.gameDate, week.playoff, games)
}

object Week {

  implicit object WeekOrdering extends Ordering[Week] {
    def compare(x: Week, y: Week) = x.gameDate.compareTo(y.gameDate)
  }

  val weekUnscheduledParser = {
    get[Pk[Long]]("week.id") ~
      long("week.season_id") ~
      date("week.game_date") ~
      bool("week.playoff") map {
      case id ~ season_id ~ game_date ~ playoff => WeekUnscheduled(id, season_id, new DateTime(game_date), playoff)
    }
  }

  val weekWithGamesParser = (weekUnscheduledParser ~ (Game.simpleParser ?))

  def processWeeks(rows: List[(WeekUnscheduled, Option[ScheduledGame])]): List[Week] =
    rows match {
      case (week, None) :: tail => week :: processWeeks(tail)
      case (week, Some(game)) :: tail => {
        val (thisWeekRows, remainingRows) = rows.span(_._1.id == week.id)
        new WeekScheduled(week, thisWeekRows map (_._2.get)) :: processWeeks(remainingRows)
      }
      case Nil => Nil
    }

  def findBySeasonId(seasonId: Long): List[Week] = DB.withConnection {
    implicit c =>
      processWeeks(
        (SQL(
          """
              SELECT * FROM week
              LEFT OUTER JOIN game ON game.week_id = week.id
              WHERE week.season_id = {season_id}
          """
        )
          .on('season_id -> seasonId)
          .as(weekWithGamesParser *) map (flatten))
      )
  }

  def create(seasonId: Long, gameDate: DateTime, playoff: Boolean = false): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into week (season_id, game_date, playoff) " +
        "values ({season_id}, {game_date}, {playoff})").
        on('season_id -> seasonId, 'game_date -> gameDate.toDate, 'playoff -> playoff)
        .executeInsert()
  }
}
