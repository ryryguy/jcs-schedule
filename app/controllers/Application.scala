package controllers

import play.api._
import db.DB
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import org.joda.time.{DateTimeConstants, LocalDate, LocalTime, DateTime}

object Application extends Controller {

  val DATE_PATTERN = "yyyy-MM-dd"
  val TIME_PATTERN = "hh:mm:ss"

  def index = Action {
    Redirect(routes.LeagueController.leagues())
  }

  def createTestData = Action {

    val league1Id = League.create(League(NotAssigned:Pk[Long], "Thursday Coed 4's", "Alki", "Sand A's and B's", true))
    val league2Id = League.create(League(NotAssigned:Pk[Long], "Thursday Coed 6's", "North Seattle", "Indoor A's and B's"))

    Logger.info("League ids: " + league1Id + ", " + league2Id)

    val lastThursday = new DateTime().withDayOfWeek(DateTimeConstants.THURSDAY).minusWeeks(1).withTime(18, 15, 0, 0)

    val weeksRegular: Byte = 3;
    val weeksPlayoffs: Byte = 1;

    val season1Id = Season.create(league1Id.get, lastThursday.toString(DATE_PATTERN), weeksRegular, weeksPlayoffs, 0, 0)
    Season.create(league2Id.get, "2012-10-04", 10, 2, 1, 0)

    val gameWeekIds = for (i <- 0 until weeksRegular + weeksPlayoffs;
                            gameDateTime = lastThursday.plusWeeks(i)) yield (
      Week.create(season1Id.get, gameDateTime, playoff = (i >= weeksRegular)).get
      )
    Logger.info("Game night ids: " + gameWeekIds)

    val teams = Array(("Shazam", "Amy Alering"), ("Shivering Chihuahuas", "Darlene O'Rourke"),
      ("Bad Feng Shui", "Mark Ninomiya"), ("USA Olympians", "Misty May")
    )

    val teamIds = for (t <- teams) yield {
      val (team, captain) = t; Team.create(team, captain, captain.takeWhile(_ != ' ') + "@gmail.com").get
    }

    Logger.info("Team ids: " + teamIds.mkString(","))

    val matches = Array(
      ((teamIds(0), teamIds(1)), (teamIds(2), teamIds(3))),
      ((teamIds(0), teamIds(2)), (teamIds(1), teamIds(3))),
      ((teamIds(0), teamIds(3)), (teamIds(1), teamIds(2)))
    )

    val matchIds = for (i <- 0 until matches.length; iMatch <- 0 to 1; val (team1, team2) = if (iMatch == 0) matches(i)._1 else matches(i)._2) yield (
      Game.create(gameWeekIds(i), new LocalTime(18 + iMatch, 15 * iMatch), iMatch + 1, team1, team2)
      )

    Logger.info("Game ids: " + matchIds.toString)
    Game.scoreSet(matchIds(0), 1, 23, 25)
    Game.scoreSet(matchIds(0), 2, 22, 25)
    Game.scoreSet(matchIds(0), 3, 25, 27)
    Game.scoreSet(matchIds(1), 1, 13, 25)
    Game.scoreSet(matchIds(1), 2, 25, 24)
    Game.scoreSet(matchIds(1), 3, 25, 17)

    Redirect(routes.LeagueController.leagues())
  }

  // For testing!!!
  def clearData = Action {
    DB.withConnection {
      implicit c =>
        SQL("delete from league;").executeUpdate(); // most other tables cleared via cascading deletes if we start at league
        SQL("delete from team;").executeUpdate();
    }

    Redirect(routes.LeagueController.leagues())
  }

  def logout = TODO
}
