package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models.{Team, Week, Season, League}
import anorm.{Id, Pk, NotAssigned}
import org.joda.time.{DateTimeZone, DateMidnight}
import controllers.Application.pkLongFormat

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/24/12
 * Time: 9:26 PM
 *
 */

object LeagueController extends Controller {
  val newLeagueForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "name" -> nonEmptyText(maxLength = 255),
      "location" -> text,
      "description" -> text,
      "active" -> boolean
    )(League.apply)(League.unapply)
  )

  def leagues = Action {
    Ok(views.html.leagues(League.active(), League.all(), newLeagueForm))
  }

  def newLeague = Action {
    implicit request =>
      newLeagueForm.bindFromRequest.fold(
        errors => BadRequest(views.html.leagues(League.active(), League.all(), newLeagueForm)),
        league => {
          League.create(league)
          Redirect(routes.LeagueController.leagues())
        }
      )
  }

  def editLeague(leagueId: Long) = Action {
    implicit request =>
      newLeagueForm.bindFromRequest.fold(
        errors => BadRequest(views.html.leagues(League.active(), League.all(), newLeagueForm)),
        league => {
          League.update(league.copy(id = Id(leagueId)))
          Ok("League updated")
        }
      )
  }

  def toggleLeague(id: Long) = Action {
    League.toggle(id)
    Redirect(routes.LeagueController.leagues())
  }

  def league(id: Long) = Action {
    val league = League.findById(id).get
    val currentSeason = Season.current(id)
    val (lastWeek, nextWeek) = if (currentSeason.isDefined) {
      val games: List[Week] = Week.findBySeasonId(currentSeason.get.id.get)
      val (b, a) = games
        .partition(p => p.gameDate.isBefore(new DateMidnight(DateTimeZone.forID("America/Los_Angeles"))))
      (if (b.isEmpty) None else Some(b.max), if (a.isEmpty) None else Some(a.min))
    } else {
      (None, None)
    }
    Ok(views.html.league(league, newLeagueForm.fill(league), Team.mapById(Team.findByLeagueId(id, false)), currentSeason, Season.next(id), lastWeek, nextWeek))
  }
}
