package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models.{Season, League}
import anorm.{Pk, NotAssigned}

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
        Redirect(routes.LeagueController.leagues)
      }
    )
  }

  def toggleLeague(id:Long) = Action {
    League.toggle(id)
    Redirect(routes.LeagueController.leagues)
  }

  def league(id:Long) = Action {
    // TODO: handle not found.
    val league:League = League.findById(id).get
    Ok(views.html.league(league, Season.current(id), Season.next(id)))
  }
}
