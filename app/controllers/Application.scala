package controllers

import play.api._
import db.DB
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.{Team, GameNight, Season, League}
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import org.joda.time.DateTime

object Application extends Controller {
  
  def index = Action {
    Redirect(routes.LeagueController.leagues())
  }

  def createTestData = Action {

    val league1Id = League.create("Thursday Coed 4's", "Alki", "Sand A's and B's", true)
    val league2Id = League.create("Thursday Coed 6's", "North Seattle", "Indoor A's and B's")

    Logger.info("League ids: " + league1Id + ", " + league2Id)

    val season1Id = Season.create(league1Id.get, "2012-06-07", 10, 3, 1, 0)
    Season.create(league2Id.get, "2012-10-04", 10, 2, 1, 0)

    val gameNightIds = for( iDayOfMonth <- 7 to(28, 7); iHour <- 18 to 19  ) yield (
      GameNight.create(season1Id.get, new DateTime(2012, 6, iDayOfMonth, iHour, if(iHour == 18) 15 else 30)).get
    )
    Logger.info("Game night ids: " + gameNightIds)

    val teams = Array(("Shazam", "Amy Alering"), ("Shivering Chihuahuas", "Darlene O'Rourke"),
        ("Bad Feng Shui", "Mark Ninomiya"),("USA Olympians", "Misty May")
    );

    val teamIds = for( t <- teams) yield {
      val (team, captain) = t; Team.create(team, captain, captain.takeWhile(_ !=' ') + "@gmail.com").get
    }


    Logger.info("Team ids: " + teamIds.mkString(","))

    Redirect(routes.LeagueController.leagues())
  }

  def logout = TODO
}