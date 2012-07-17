package model

import anorm.{Pk, NotAssigned}
import models._
import org.joda.time.{LocalTime, DateTime}
import org.specs2.execute.{Result, Failure, Pending}
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

class GameModelTest extends Specification {
  // set up some games
  running(FakeApplication()) {
    val leagueId: Long = League.create(League(NotAssigned: Pk[Long], "Season test league", "Season test location", "Season test")).get
    val seasonStartDate: DateTime = new DateTime().minusWeeks(1)
    val seasonId: Long = Season.create(leagueId, seasonStartDate, 3, 1, 1, 1).get
    val week2WithGamesId: Long = Week.create(seasonId, seasonStartDate.plusWeeks(1), playoff = false).get
    val week1WithGamesId: Long = Week.create(seasonId, seasonStartDate, playoff = false).get

    val teamIds: Seq[Long] = Application.createTestTeams(leagueId)

    val week2Game2Id = Game.create(week2WithGamesId, new LocalTime(18, 0), 2, teamIds(2), teamIds(3))
    val week1Game1Id = Game.create(week1WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week2Game1Id = Game.create(week2WithGamesId, new LocalTime(18, 0), 1, teamIds(0), teamIds(1))
    val week1Game2Id = Game.create(week1WithGamesId, new LocalTime(19, 0), 1, teamIds(2), teamIds(3))

    Game.scoreSet(week1Game1Id, 1, 25, 23)
    Game.scoreSet(week1Game1Id, 3, 25, 13)
    Game.scoreSet(week1Game1Id, 2, 22, 25)

    Game.scoreSet(week1Game2Id, 3, 18, 25)
    Game.scoreSet(week1Game2Id, 1, 22, 25)
    Game.scoreSet(week1Game2Id, 2, 25, 27)

    val gamesForWeek1: List[Game] = Game.findByWeekId(week1WithGamesId)
    val gamesForWeek2: List[Game] = Game.findByWeekId(week2WithGamesId)

    "The Game model interface" should {

      "retrieve all games for a week" in {
        gamesForWeek1 must have size 2
        gamesForWeek1 map (_.id.get) must contain(week1Game1Id, week1Game2Id)

        gamesForWeek2 must have size 2
        gamesForWeek2 map (_.id.get) must contain(week2Game1Id, week2Game2Id)
      }

      "return games in chronological then court order" in {
        gamesForWeek1 map (_.id.get) must contain(week1Game1Id, week1Game2Id).inOrder
        gamesForWeek2 map (_.id.get) must contain(week2Game1Id, week2Game2Id).inOrder
      }

      "return a Game with unfinished sets as a ScheduledGame" in {
        gamesForWeek2.forall(game => game must haveClass(classManifest[ScheduledGame]))
      }

      "return a Game with finished sets as a CompletedGame" in {
        gamesForWeek1.forall(game => game must haveClass(classManifest[CompletedGame]))
      }

      "indicate the correct team wins for a CompletedGame" in {
        gamesForWeek1(0).asInstanceOf[CompletedGame].team1Wins aka "Game 1 Team 1 wins" must_== 2
        gamesForWeek1(0).asInstanceOf[CompletedGame].team2Wins aka "Game 1 Team 2 wins" must_== 1

        gamesForWeek1(1).asInstanceOf[CompletedGame].team1Wins aka "Game 2 Team 1 wins" must_== 0
        gamesForWeek1(1).asInstanceOf[CompletedGame].team2Wins aka "Game 2 Team 2 wins" must_== 3
      }

      "indicate the correct set scores for a CompletedGame" in {
        gamesForWeek1(0).asInstanceOf[CompletedGame].setScores aka "Game 1 set scores" must contain("25-23", "22-25", "25-13").inOrder and have size 3
        gamesForWeek1(1).asInstanceOf[CompletedGame].setScores aka "Game 2 set scores" must contain("22-25", "25-27", "18-25").inOrder and have size 3
      }
    }

  }
}
