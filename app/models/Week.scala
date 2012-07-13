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

abstract class Week() {
  def id: Pk[Long]
  def gameDate: DateTime
}

case class WeekUnscheduled(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean) extends Week
case class WeekScheduled(id: Pk[Long] = NotAssigned, seasonId: Long, gameDate: DateTime, playoff: Boolean, games: Seq[ScheduledGame]) extends Week

object Week {

  implicit object WeekOrdering extends Ordering[Week] {
    def compare(x: Week, y: Week) = x.gameDate.compareTo(y.gameDate)
  }

  val weekUnscheduledParser = {
    get[Pk[Long]]("week.id") ~
      long("week.season_id") ~
      date("week.game_date") ~
      bool("week.playoff") map {
      case id ~ season_id ~ game_date ~ playoff => new WeekUnscheduled(id, season_id, new DateTime(game_date), playoff)
    }
  }

  val weekWithGamesParser = (weekUnscheduledParser ~ (Game.simpleParser ?))

//  def findBySeasonId(seasonId: Long): List[Week] = DB.withConnection {
//    implicit c =>
//      val rs: Stream[Row] =
//        SQL(
//          """
//          SELECT * FROM week
//          LEFT OUTER JOIN game ON game.week_id = week.id
//          WHERE week.season_id = {season_id}
//          """
//        )
//          .on('season_id -> seasonId)()
//
//      def process(rows : Stream[Row]) : List[Week] = rows match {
//        case Stream.Empty => Nil
//        case row #:: tail => {
//        val w:Week = row.
//        }
//
//    }
//
//
//      ((rs.groupBy(_._1).mapValues(l => l map (_._2)))
//        map {
//          case (w, g) if g.head == None => w
//          case (w, g)                   => WeekScheduled(w, g.map(_.get))
//        }
//      ).toList
//  }

  def findBySeasonId(seasonId: Long): List[Week] = DB.withConnection {
        implicit c =>
          val rs: List[(WeekUnscheduled, Option[ScheduledGame])] =
            SQL(
              """
              SELECT * FROM week
              LEFT OUTER JOIN game ON game.week_id = week.id
              WHERE week.season_id = {season_id}
              """
            )
              .on('season_id -> seasonId)
              .as(weekWithGamesParser *) map (flatten)

          ((rs.groupBy(_._1).mapValues(l => l map (_._2)))
            map {
              case (w, g) if g.head == None => w
              case (w, g)                   => WeekScheduled(w, g.map(_.get))
            }
          ).toList
      }

  def create(seasonId: Long, gameDate: DateTime, playoff: Boolean = false): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into week (season_id, game_date, playoff) " +
        "values ({season_id}, {game_date}, {playoff})").
        on('season_id -> seasonId, 'game_date -> gameDate.toDate, 'playoff -> playoff)
        .executeInsert()
  }

  object WeekScheduled {
    def apply(week: WeekUnscheduled, games: Seq[ScheduledGame]) =
      new WeekScheduled(week.id, week.seasonId, week.gameDate, week.playoff, games)

  }

}
