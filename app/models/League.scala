package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

/**
 * League: top-level model of the schema.
 *
 * User: Ryan
 * Date: 6/10/12
 * Time: 10:16 PM
 */

case class League(id: Long = -1, name: String, location: String, description: String, active: Boolean = false)

object League {
  val league = {
    get[Long]("id") ~
      get[String]("league_name") ~
      get[String]("location") ~
      get[String]("description") ~
      get[Boolean]("active") map {
      case id ~ league_name ~ location ~ description ~ active => League(id, league_name, location, description, active)
    }
  }

  def all(): List[League] = DB.withConnection {
    implicit c =>
      SQL("select * from league").as(league *)
  }

  def active(): List[League] = DB.withConnection {
    implicit c =>
      SQL("select * from league where active = true").as(league *)
  }

  def create(name: String, location: String, description: String) {
    DB.withConnection {
      implicit c =>
        SQL("insert into league (league_name, location, description) " +
          "values ({league_name}, {location}, {description})").
          on('league_name -> name, 'location -> location, 'description -> description)
          .executeUpdate()
    }
  }

  def toggle(lid: Long) = {
    DB.withConnection {
      implicit c =>
        SQL("update league set active = not active " +
          "where id = " + lid)
          .executeUpdate()
    }
  }
}
