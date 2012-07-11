import anorm.{Pk, NotAssigned}
import models.League
import org.specs2.mutable.Specification
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/10/12
 * Time: 10:19 PM
 */

class LeagueTest extends Specification {

  "League instances" should {

    "not have any with an empty database" in {
      running(FakeApplication()) {
        controllers.Application.clearData()(FakeRequest())
        League.any() must beFalse
      }
    }

    "be retrievable by id" in {
      running(FakeApplication()) {
        val leagueId = League.create(League(NotAssigned: Pk[Long], "Test league", "Test location", "Test description"))
        leagueId should not be None

        val league = League.findById(leagueId.get)
        league aka "the retrieved league" must beSome
        league.get.name must_== "Test league"
        league.get.location must_== "Test location"
        league.get.description must_== "Test description"
      }
    }

    "all be retrievable in a list" in {
      running(FakeApplication()) {
        League.create(League(NotAssigned: Pk[Long], "Test league 2", "Test location", "Test description"))

        val allLeagues: List[League] = League.all()
        allLeagues must have size 2
        allLeagues(1).name must_== "Test league 2"
      }
    }

    "be toggleable between active and inactive" in {
      running(FakeApplication()) {
        val leagueToToggle: League = League.all().head
        leagueToToggle.active must beFalse

        League.toggle(leagueToToggle.id.get)
        League.findById(leagueToToggle.id.get) should beSome.which(_.active must beTrue)
      }
    }

    "be retrievable by active leagues only" in {
      running(FakeApplication()) {
        League.active() must have size 1
        League.active().head.active must beTrue
      }
    }
  }
}
