package model

import anorm.{Pk, NotAssigned}
import models._
import models.CurrentSeason
import org.joda.time.{LocalTime, DateTime}
import org.specs2.execute.{Result, Success, Failure, Pending}
import org.specs2.mutable.Specification
import play.api.test.FakeApplication
import play.api.test.Helpers._
import controllers.Application

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/11/12
 * Time: 9:01 PM
 */

class WeekModelTest extends Specification {
  // set up season with weeks
  running(FakeApplication()) {
    val leagueId: Long = League.create(League(NotAssigned: Pk[Long], "Season test league", "Season test location", "Season test")).get
    val seasonStartDate: DateTime = new DateTime().minusWeeks(1)
    val seasonId: Long = Season.create(leagueId, seasonStartDate, 3, 1, 1, 1).get
    val week2WithGamesId: Long = Week.create(seasonId, seasonStartDate.plusWeeks(1), playoff = false).get
    val week3WithoutGamesId: Long = Week.create(seasonId, seasonStartDate.plusWeeks(2), playoff = false).get
    val week1WithGamesId: Long = Week.create(seasonId, seasonStartDate, playoff = false).get
    val week4WithoutGamesId: Long = Week.create(seasonId, seasonStartDate.plusWeeks(3), playoff = true).get

    val weekInOtherSeason = Week.create(Season.create(leagueId, seasonStartDate.plusMonths(4), 3, 1, 1, 1).get, seasonStartDate.plusMonths(4), false)

    val teamIds = Application.createTestTeams(leagueId)

    val week1Game1Id = Game.create(week1WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week1Game2Id = Game.create(week1WithGamesId, new LocalTime(18, 0), 1, teamIds(2), teamIds(3))
    val week2Game1Id = Game.create(week2WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week2Game2Id = Game.create(week2WithGamesId, new LocalTime(18, 0), 1, teamIds(2), teamIds(3))

    val seasonWeeks: List[Week] = Week.findBySeasonId(seasonId)

    "The Week model interface" should {
      "retrieve all weeks for a season" in {
        seasonWeeks must have size 4
        seasonWeeks map (_.id.get) must contain(week1WithGamesId, week2WithGamesId, week3WithoutGamesId, week4WithoutGamesId)
      }

      "return them in chronological order" in {
        seasonWeeks map (_.id.get) must contain(week1WithGamesId, week2WithGamesId, week3WithoutGamesId, week4WithoutGamesId).inOrder
      }.pendingUntilFixed

      "return a Week without any Games as a WeekUnscheduled" in {
        seasonWeeks.find(_.id.get == week3WithoutGamesId).get must haveClass(classManifest[WeekUnscheduled])
        seasonWeeks.find(_.id.get == week4WithoutGamesId).get must haveClass(classManifest[WeekUnscheduled])
      }

      "return a Week with Games as a WeekScheduled containing a list of the Games" in {
        val week1Result: Result = seasonWeeks.find(_.id.get == week1WithGamesId) match {
          case None => Failure("Couldn't find week1")
          case Some(w: WeekScheduled) => w.games map (_.id.get) must contain(week1Game1Id, week1Game2Id)
          case _ => Failure("Week1 not a WeekScheduled?")
        }

        val week2Result: Result = seasonWeeks.find(_.id.get == week2WithGamesId) match {
          case None => Failure("Couldn't find week2")
          case Some(w: WeekScheduled) => w.games map (_.id.get) must contain(week2Game1Id, week2Game2Id)
          case _ => Failure("Week2 not a WeekScheduled?")
        }

        week1Result and week2Result
      }

      "return a Week with completed Games as a WeekCompleted containing a list of the Games" in {
        todo
      }

      "not allow creation of a Week dated before the Season starts" in {
        Pending("Maybe")
      }

      "not allow creation of a Week dated after the Season ends" in {
        Pending("Maybe")
      }

    }

  }
}
