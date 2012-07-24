package controller

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.FakeApplication
import models._
import anorm.{Pk, NotAssigned}
import org.joda.time.{LocalTime, DateTime}
import controllers.{WinLossRecord, StandingsLine, SeasonController, Application}
import play.api.test.FakeApplication

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/22/12
 * Time: 9:25 PM
 */

class SeasonControllerTest extends Specification {
  // set up some games
  running(FakeApplication()) {
    val leagueId: Long = League.create(League(NotAssigned: Pk[Long], "Season controller test league", "Season test location", "Season controller test")).get
    val seasonStartDate: DateTime = new DateTime().minusWeeks(1)
    val seasonId: Long = Season.create(leagueId, seasonStartDate, 3, 1, 1, 1).get
    val week2WithGamesId: Long = Week.create(seasonId, seasonStartDate.plusWeeks(1), playoff = false).get
    val week1WithGamesId: Long = Week.create(seasonId, seasonStartDate, playoff = false).get

    val teamIds: Seq[Long] = Application.createTestTeams(leagueId)

    val week1Game1Id = Game.create(week1WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week1Game2Id = Game.create(week1WithGamesId, new LocalTime(19, 0), 1, teamIds(2), teamIds(3))
    // dummy weeks - make sure they are not considered
    Game.create(week2WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    Game.create(week2WithGamesId, new LocalTime(18, 0), 2, teamIds(2), teamIds(3))

    Game.scoreSet(week1Game1Id, 1, 25, 23)
    Game.scoreSet(week1Game1Id, 3, 25, 13)
    Game.scoreSet(week1Game1Id, 2, 22, 25)

    Game.scoreSet(week1Game2Id, 3, 18, 25)
    Game.scoreSet(week1Game2Id, 1, 22, 25)
    Game.scoreSet(week1Game2Id, 2, 25, 27)

    val standings: Iterable[StandingsLine] = SeasonController.calculateStandings(seasonId)

    "The SeasonController calculate standings method" should {

      "Calculate a standing line for each team" in {
        standings must have size teamIds.size
      }

      "Calculate correct win loss records for each team" in {
        val winLossRecords: Map[Long, WinLossRecord] = standings.groupBy(_.teamId).mapValues(sl => WinLossRecord(sl.head.wins, sl.head.losses))
        winLossRecords must havePairs(
          (teamIds(0) -> WinLossRecord(2, 1)),
          (teamIds(1) -> WinLossRecord(1, 2)),
          (teamIds(2) -> WinLossRecord(0, 3)),
          (teamIds(3) -> WinLossRecord(3, 0)))
      }

      "Calculate correct place for each team when there are no ties" in {
        standings must contain(
          StandingsLine(1, teamIds(3), 3, 0),
          StandingsLine(2, teamIds(0), 2, 1),
          StandingsLine(3, teamIds(1), 1, 2),
          StandingsLine(4, teamIds(2), 0, 3)
        )
      }

      "Break ties using head to head matchups" in {
        todo
      }

      "Indicate ties where head to head matchups are equal" in {
        todo
      }

      "Indicate ties where multiple head to head matchups are equal" in {
        // i.e, A, B and C have the same record, in head-to-head A > B, B > C, C > A
        todo
      }

      "Calculate correct places when there are an unequal number of games played due to byes" in {
        // should work already I think?
        todo
      }

      "Break ties correctly when there are an unequal number of games played due to byes" in {
        // might just work when tie breaks implemented
        todo
      }

    }
  }
}
