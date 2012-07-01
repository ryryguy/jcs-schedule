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

case class Set(num: Byte, matchId: Long, team1Score: Option[Short], team2Score: Option[Short])

object Set {
  // magic from google group
  implicit def rowToByte: Column[Byte] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case byte: Byte => Right(byte)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Byte for column " + qualified))
    }
  }

  val setParser = {
    get[Byte]("num") ~
      long("match_id") ~
      get[Option[Short]]("team1_score") ~
      get[Option[Short]]("team2_score") map {
      case num ~ match_id ~ team1_score ~ team2_score => new Set(num, match_id, team1_score, team2_score)
    }
  }

  def create(num:Byte, matchId: Long, team1Score: Option[Short] = None, team2Score: Option[Short] = None): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into set (num, match_id, team1_score, team2_score) " +
        "values ({num}, {match_id}, {team1_score}, {team2_score})").
        on('num -> num, 'match_id -> matchId, 'team1_score -> team1Score, 'team2_score -> team2Score)
        .executeInsert()
  }
}