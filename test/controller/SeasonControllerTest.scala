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
        val winLossRecords: Map[Long, WinLossRecord] = standings.groupBy(_.teamId).mapValues(sl => WinLossRecord(sl.head.teamId, sl.head.wins, sl.head.losses))
        winLossRecords must havePairs(
          (teamIds(0) -> WinLossRecord(teamIds(0), 2, 1)),
          (teamIds(1) -> WinLossRecord(teamIds(1), 1, 2)),
          (teamIds(2) -> WinLossRecord(teamIds(2), 0, 3)),
          (teamIds(3) -> WinLossRecord(teamIds(3), 3, 0)))
      }

      "Calculate correct place for each team when there are no ties" in {
        standings must contain(
          StandingsLine(teamIds(3), 3, 0),
          StandingsLine(teamIds(0), 2, 1),
          StandingsLine(teamIds(1), 1, 2),
          StandingsLine(teamIds(2), 0, 3)
        ).inOrder
      }

      "Break two-team ties using head to head matchups" in {
        val games = List(
          makeGame(1, 2, 3, 0),
          makeGame(3, 4, 2, 1),
          makeGame(2, 3, 3, 0),
          makeGame(4, 1, 3, 0)
        );

        SeasonController.calculateHeadToHead(games, SeasonController.calculateWinLoss(games).toSeq) must contain(
          StandingsLine(4, 4, 2),
          StandingsLine(1, 3, 3, Some(1)),
          StandingsLine(2, 3, 3, Some(2)),
          StandingsLine(3, 2, 4)
        ).inOrder

      }

      "Indicate two-team ties stay tied where head to head matchups are equal" in {
        val games = List(
          makeGame(1, 2, 3, 0),
          makeGame(3, 4, 1, 2),
          makeGame(4, 3, 3, 0),
          makeGame(2, 1, 3, 0)
        );

        SeasonController.calculateHeadToHead(games, SeasonController.calculateWinLoss(games).toSeq) must contain(
          StandingsLine(4, 5, 1),
          StandingsLine(1, 3, 3, Some(1)),
          StandingsLine(2, 3, 3, Some(1)),
          StandingsLine(3, 1, 5)
        ).inOrder
      }

      "Indicate two-team ties stay tied where head to head matchups don't exist" in {
        val games = List(
          makeGame(1, 4, 3, 0),
          makeGame(2, 4, 1, 2),
          makeGame(1, 3, 1, 2),
          makeGame(2, 3, 3, 0),
          makeGame(3, 4, 3, 0)
        );

        SeasonController.calculateHeadToHead(games, SeasonController.calculateWinLoss(games).toSeq) must contain(
          StandingsLine(1, 4, 2, Some(-1)),
          StandingsLine(2, 4, 2, Some(-1)),
          StandingsLine(3, 5, 4),
          StandingsLine(4, 2, 7)
        ).inOrder
      }

      "Indicate three+-team ties stay tied (for now)" in {
        val games = List(
          makeGame(1, 4, 3, 0),
          makeGame(2, 3, 1, 2),
          makeGame(1, 3, 1, 2),
          makeGame(2, 4, 3, 0),
          makeGame(3, 4, 2, 1)
        );

        SeasonController.calculateHeadToHead(games, SeasonController.calculateWinLoss(games).toSeq) must contain(
          StandingsLine(1, 4, 2, Some(-1)),
          StandingsLine(2, 4, 2, Some(-1)),
          StandingsLine(3, 6, 3, Some(-1)),
          StandingsLine(4, 1, 8)
        ).inOrder
      }

      "Break three-team ties where one team beat the other two, falling back to two-team rules for the remainder" in {
        todo
      }

      "Indicate three-team ties stay tied when no one team beats both others head-to-head" in {
        // i.e, A, B and C have the same record, in head-to-head A > B, B > C, C > A
        todo
      }

      "Indicate four+-team ties stay tied (for now?)" in {
        todo
      }

      "Break multiple two-way ties" in {
        val games = List(
          makeGame(1, 2, 3, 0),
          makeGame(3, 4, 2, 1),
          makeGame(4, 3, 3, 0),
          makeGame(2, 1, 3, 0),
          makeGame(3, 5, 3, 0),
          makeGame(4, 5, 1, 2)
        );

        SeasonController.calculateHeadToHead(games, SeasonController.calculateWinLoss(games).toSeq) must contain(
          StandingsLine(4, 5, 4, Some(1)),
          StandingsLine(3, 5, 4, Some(2)),
          StandingsLine(1, 3, 3, Some(1)),
          StandingsLine(2, 3, 3, Some(1)),
          StandingsLine(5, 2, 4)
        ).inOrder
      }
    }
  }

  def makeGame(team1: Long, team2: Long, team1Wins: Int, team2Wins: Int) = CompletedGame(NotAssigned, 0, team1, team2, team1Wins, team2Wins, List())
}
