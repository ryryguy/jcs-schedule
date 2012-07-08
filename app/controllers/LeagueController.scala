package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models.{Team, Week, Season, League}
import anorm.{Pk, NotAssigned}
import org.joda.time.DateMidnight

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/24/12
 * Time: 9:26 PM
 *
 */

object LeagueController extends Controller {
  val leagueForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText(maxLength=255),
      "location" -> text,
      "description" -> text,
      "active" -> boolean
    )(League.apply)(League.unapply)
  )

  def leagues = Action {
    Ok(views.html.leagues(League.active(), League.all(), leagueForm))
  }

  def newLeague =  Action { implicit request =>
    leagueForm.bindFromRequest.fold(
      errors => BadRequest(views.html.leagues(League.active(), League.all(), leagueForm)),
      league => {
        League.create(league)
        Redirect(routes.LeagueController.leagues())
      }
    )
  }

  def toggleLeague(id:Long) = Action {
    League.toggle(id)
    Redirect(routes.LeagueController.leagues())
  }

//  implicit object WeekWithGamesOrdering extends Ordering[WeekWithGames] {
//    def compare(x: WeekWithGames, y: WeekWithGames) = x.week.gameDate.compareTo(y.week.gameDate)
//  }

  def league(id:Long) = Action {
    val league = League.findById(id).get
    val currentSeason = Season.current(id)
    val (lastWeek, nextWeek) = if(currentSeason.isDefined) {
      val games: List[Week] = Week.findBySeasonId(currentSeason.get.id.get)
      val (b, a) = games
          .partition(p => p.gameDate.isBefore(new DateMidnight()))
      (if(b.isEmpty) None else Some(b.max), if(a.isEmpty) None else Some(a.min))
    } else {
      (None, None)
    }
    Ok(views.html.league(league, Team.findByLeagueId(id, false).groupBy(_.id.get).mapValues(_.head), currentSeason, Season.next(id), lastWeek, nextWeek))
  }
}
