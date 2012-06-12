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

case class League(id: Int, name: String, location: String, description: String, active: Boolean)

object League {
  def create(name: String, location: String, description: String) {
    DB.withConnection { implicit c =>
      SQL("insert into league (name, location, description) " +
        "values {name, location, description, active}").on(
        'name -> name, 'location -> location, 'description ->description
        ).executeUpdate()
    }
  }
}
