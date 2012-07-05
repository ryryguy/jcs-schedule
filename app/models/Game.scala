package models

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */

import org.joda.time.{LocalTime, LocalDate, DateTime, DateMidnight}
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current
import controllers.Application

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */

case class Game(id: Pk[Long] = NotAssigned, weekId: Long, startTime: LocalTime, court: Int, team1Id: Long, team2Id: Long,
                numSets: Int)

object Game extends ByteParser {
  val simpleParser = {
    get[Pk[Long]]("game.id") ~
      long("game.week_id") ~
      date("game.start_time") ~
      get[Byte]("game.court") ~
      long("game.team1_id") ~
      long("game.team2_id") ~
      get[Byte]("game.num_sets") map {
      case id ~ week_id ~ start_time ~ court ~ team1_id ~ team2_id ~ num_sets
      => new Game(id, week_id, LocalTime.fromDateFields(start_time), court, team1_id, team2_id, num_sets)
    }
  }

  def create(weekId: Long, startTime: LocalTime, court: Int, team1Id: Long, team2Id: Long,
             numSets: Int = 3, playoff: Boolean = false, createSets: Boolean = true): Long = DB.withConnection {
    implicit c =>
      val matchId: Long = SQL(
        """
        insert into game (week_id, start_time, court, team1_id, team2_id, num_sets)
        values ({week_id}, {start_time}, {court}, {team1_id}, {team2_id}, {num_sets})
        """)
        .on('week_id -> weekId, 'start_time -> startTime.toString(Application.TIME_PATTERN), 'court -> court,
        'team1_id -> team1Id, 'team2_id -> team2Id, 'num_sets -> numSets)
        .executeInsert().get

      if (createSets) {
        for (i <- 1 to numSets) {
          Set.create(i.toByte, matchId)
        }
      }

      matchId
  }

  def scoreSet(matchId: Long, setNum: Byte, team1Score: Byte, team2Score: Byte) = DB.withConnection {
    implicit c =>
      SQL( """ update set
          set team1_score = {team1_score}, team2_score = {team2_score}
          where num = {num} and game_id = {game_id}
           """)
        .on('team1_score -> team1Score, 'team2_score -> team2Score, 'num -> setNum, 'game_id -> matchId)
        .executeUpdate()
  }
}
