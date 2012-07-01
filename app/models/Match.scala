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

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */

case class Match(id: Long, gameNightId: Long, startTime: LocalTime, court: Int, team1Id: Long, team2Id: Long,
                 numSets: Int)

object Match {
  val matchParser = {
    long("id") ~
      long("game_night_id") ~
      date("start_time") ~
      int("court") ~
      long("team1_id") ~
      long("team2_id") ~
      int("num_sets") map {
      case id ~ game_night_id ~ start_time ~ court ~ team1_id ~ team2_id ~ num_sets
      => new Match(id, game_night_id, LocalTime.fromDateFields(start_time), court, team1_id, team2_id, num_sets)
    }
  }

  def create(gameNightId: Long, startTime: LocalTime, court: Int, team1Id: Long, team2Id: Long,
             numSets: Int = 3, playoff: Boolean = false, createSets: Boolean = true): Long = DB.withConnection {
    implicit c =>
      val matchId: Long = SQL("insert into match (game_night_id, start_time, court, team1_id, team2_id, num_sets) " +
        "values ({game_night_id}, {start_time}, {court}, {team1_id}, {team2_id}, {num_sets})").
        on('game_night_id -> gameNightId, 'start_time -> startTime.toString("hh:mm:ss"), 'court -> court,
        'team1_id -> team1Id, 'team2_id -> team2Id, 'num_sets -> numSets)
        .executeInsert().get

      if (createSets) {
        for (i <- 1 to numSets) {
          Set.create(i.toByte, matchId)
        }
      }

      matchId
  }

  def scoreSet(matchId : Long, setNum: Byte, team1Score: Byte, team2Score: Byte) = DB.withConnection {
    implicit c =>
      SQL(""" update set
          set team1_score = {team1_score}, team2_score = {team2_score}
          where num = {num} and match_id = {match_id}
          """)
        .on('team1_score -> team1Score, 'team2_score -> team2Score, 'num -> setNum, 'match_id -> matchId)
        .executeUpdate()
  }
}

