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

case class League(id: Long, name: String, location: String, description: String, active: Boolean = false)

object League {
  val league = {
      long("id") ~
      str("league_name") ~
      str("location") ~
      str("description") ~
      bool("active") map {
      case id ~ league_name ~ location ~ description ~ active => League(id, league_name, location, description, active)
    }
  }

  def any(): Boolean = DB.withConnection {
    implicit c =>
      SQL("select count(*) > 0 from league").as(scalar[Boolean].single)
  }

  def all(): List[League] = DB.withConnection {
    implicit c =>
      SQL("select * from league").as(league *)
  }

  def get(id:Long):League = DB.withConnection {
    implicit c =>
      SQL("select * from league where id = " + id).as(league.single)
  }

  def active(): List[League] = DB.withConnection {
    implicit c =>
      SQL("select * from league where active = true").as(league *)
  }

  def create(name: String, location: String, description: String, active: Boolean = false) : Option[Long] = DB.withConnection {
      implicit c =>
        SQL("insert into league (league_name, location, description, active) " +
          "values ({league_name}, {location}, {description}, {active})").
          on('league_name -> name, 'location -> location, 'description -> description, 'active -> active)
          .executeInsert()
  }

  def toggle(lid: Long) = DB.withConnection {
      implicit c =>
        SQL("update league set active = not active " +
          "where id = " + lid)
          .executeUpdate()
  }
}
