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
    val week1WithCompletedGamesId: Long = Week.create(seasonId, seasonStartDate, playoff = false).get
    val week4WithoutGamesId: Long = Week.create(seasonId, seasonStartDate.plusWeeks(3), playoff = true).get

    val weekInOtherSeason = Week.create(Season.create(leagueId, seasonStartDate.plusMonths(4), 3, 1, 1, 1).get, seasonStartDate.plusMonths(4), false)

    val teamIds = Application.createTestTeams(leagueId)

    val week1Game1Id = Game.create(week1WithCompletedGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week1Game2Id = Game.create(week1WithCompletedGamesId, new LocalTime(18, 0), 1, teamIds(2), teamIds(3))
    val week2Game1Id = Game.create(week2WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week2Game2Id = Game.create(week2WithGamesId, new LocalTime(18, 0), 1, teamIds(2), teamIds(3))

    Game.scoreSet(week1Game1Id, 1, 25, 23)
    Game.scoreSet(week1Game1Id, 3, 25, 13)
    Game.scoreSet(week1Game1Id, 2, 22, 25)

    Game.scoreSet(week1Game2Id, 3, 18, 25)
    Game.scoreSet(week1Game2Id, 1, 22, 25)
    Game.scoreSet(week1Game2Id, 2, 25, 27)

    val seasonWeeks: List[Week] = Week.findBySeasonId(seasonId)

    "The Week model interface" should {
      "retrieve all weeks for a season" in {
        seasonWeeks must have size 4
        seasonWeeks map (_.id.get) must contain(week1WithCompletedGamesId, week2WithGamesId, week3WithoutGamesId, week4WithoutGamesId)
      }

      "return them in chronological order" in {
        seasonWeeks map (_.id.get) must contain(week1WithCompletedGamesId, week2WithGamesId, week3WithoutGamesId, week4WithoutGamesId).inOrder
      }

      "return a Week without any Games as a WeekUnscheduled" in {
        seasonWeeks.find(_.id.get == week3WithoutGamesId).get must haveClass(classManifest[WeekUnscheduled])
        seasonWeeks.find(_.id.get == week4WithoutGamesId).get must haveClass(classManifest[WeekUnscheduled])
      }

      "return a Week with Scheduled Games as a WeekScheduled containing a list of the Scheduled Games" in {
        seasonWeeks.find(_.id.get == week2WithGamesId) must beSome.which(_.isInstanceOf[WeekScheduled])
        val actualGameIds = seasonWeeks.find(_.id.get == week2WithGamesId).get.asInstanceOf[WeekScheduled].games map (_.id.get)
        actualGameIds must have size 2 and contain(week2Game1Id, week2Game2Id)
      }

      "return a Week with completed Games as a WeekCompleted containing a list of the Completed Games with tallied wins and set secores" in {
        seasonWeeks.find(_.id.get == week1WithCompletedGamesId) must beSome.which(_.isInstanceOf[WeekCompleted])
        val actualGames: Seq[CompletedGame] = seasonWeeks.find(_.id.get == week1WithCompletedGamesId).get.asInstanceOf[WeekCompleted].games
        actualGames map (_.id.get) must have size 2 and contain(week1Game1Id, week1Game2Id)
        val game1 = actualGames(0)
        game1.team1Wins must_== 2
        game1.team2Wins must_== 1
        game1.setScores must contain("25-23", "22-25", "25-13").inOrder
        val game2 = actualGames(1)
        game2.team1Wins must_== 0
        game2.team2Wins must_== 3
        game2.setScores must contain("22-25", "25-27", "18-25").inOrder
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
