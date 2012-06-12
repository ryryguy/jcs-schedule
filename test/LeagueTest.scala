import org.specs2.mutable.Specification
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/10/12
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */

class LeagueTest extends Specification  {

  "respond to the addLeague Action" in {
    val result = controllers.Application.newLeague()(FakeRequest())

    status(result) must equalTo(OK)
  }
}
