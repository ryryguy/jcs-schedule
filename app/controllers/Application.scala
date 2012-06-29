package controllers

import play.api._
import db.DB
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.{Season, League}
import anorm._
import anorm.SqlParser._
import play.api.Play.current

object Application extends Controller {
  
  def index = Action {
    Redirect(routes.LeagueController.leagues())
  }

  def createTestData = Action {

    League.create("Thursday Coed 4's", "Alki", "Sand A's and B's", true)
    League.create("Thursday Coed 6's", "North Seattle", "Indoor A's and B's")

    val leagues = League.all()
    Season.create(leagues.head.id, "2012-06-07", 10, 2, 1, 0)
    Season.create(leagues.tail.head.id, "2012-10-04", 10, 2, 1, 0)

    Redirect(routes.LeagueController.leagues())
  }

  def logout = TODO
}