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

case class Team (id: Long = -1, name:String, captainName: String, captainEmail: String )

object Team {
  val team = {
      long("id") ~
      str("name") ~
      str("captain_name") ~
      str("captain_email") map {
      case id ~ name ~ captain_name ~ captain_email => new Team(id, name, captain_name, captain_email)
    }
  }

  def create(name:String, captainName: String, captainEmail: String) : Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into team (name, captain_name, captain_email) " +
        "values ({name}, {captain_name}, {captain_email})").
        on('name -> name, 'captain_name -> captainName, 'captain_email -> captainEmail)
        .executeInsert()
  }
}
