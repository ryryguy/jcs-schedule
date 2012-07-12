import anorm.{Pk, NotAssigned}
import models._
import models.CurrentSeason
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import play.api.test.FakeApplication
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/11/12
 * Time: 9:01 PM
 */

class SeasonModelTest extends Specification {
  "Set up league with seasons"
  running(FakeApplication()) {
    val leagueId: Long = League.create(League(NotAssigned: Pk[Long], "Season test league", "Season test location", "Season test")).get
    val seasonStartedLastWeekId: Long = Season.create(leagueId, new DateTime().minusWeeks(1), 10, 2, 1, 1).get
    val seasonStartingInSixMonthsId: Long = Season.create(leagueId, new DateTime().plusMonths(6), 10, 2, 1, 1).get
    val seasonStartingNextMonthId: Long = Season.create(leagueId, new DateTime().plusMonths(1), 10, 2, 1, 1).get

    val leagueSeasons: List[LeagueSeason] = Season.getForLeague(leagueId)

    "The Season model interface" should {
      "retrieve all seasons for a league" in {
        leagueSeasons must have size 3 //and(have(_.s.id == seasonStartedLastWeekId))
        leagueSeasons map (_.s.id.get) must contain(seasonStartedLastWeekId, seasonStartingNextMonthId, seasonStartingInSixMonthsId)
      }

      "return an uncompleted, started season as a CurrentSeason" in {
        leagueSeasons.find(_.s.id.get == seasonStartedLastWeekId).get must haveClass(classManifest[CurrentSeason])
      }

      "return uncompleted, unstarted seasons as NextSeasons" in {
        leagueSeasons.find(_.s.id.get == seasonStartingInSixMonthsId).get must haveClass(classManifest[NextSeason])
        leagueSeasons.find(_.s.id.get == seasonStartingNextMonthId).get must haveClass(classManifest[NextSeason])
      }

      "return completed seasons as CompletedSeasons" in {
        todo
      }

      "retrieve the current season" in {
        running(FakeApplication()) {
          Season.current(leagueId) should beSome[Season].which(_.id.get == seasonStartedLastWeekId)
        }
      }

      "retrieve the earliest next season" in {
        running(FakeApplication()) {
          Season.next(leagueId) should beSome[Season].which(_.id.get == seasonStartingNextMonthId)
        }
      }

      "not allow creation of a second current season" in {
        todo
      }
    }
  }
}
