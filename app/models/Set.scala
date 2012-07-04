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

case class Set(num: Byte, gameId: Long, team1Score: Option[Short], team2Score: Option[Short])

object Set extends ByteParser {
  val setParser = {
    get[Byte]("num") ~
      long("game_id") ~
      get[Option[Short]]("team1_score") ~
      get[Option[Short]]("team2_score") map {
      case num ~ game_id ~ team1_score ~ team2_score => new Set(num, game_id, team1_score, team2_score)
    }
  }

  def create(num:Byte, gameId: Long, team1Score: Option[Short] = None, team2Score: Option[Short] = None): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into set (num, game_id, team1_score, team2_score) " +
        "values ({num}, {game_id}, {team1_score}, {team2_score})").
        on('num -> num, 'game_id -> gameId, 'team1_score -> team1Score, 'team2_score -> team2Score)
        .executeInsert()
  }
}
