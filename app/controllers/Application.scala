package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.League

object Application extends Controller {
  
  def index = Action {
    Redirect(routes.Application.leagues())
  }

  val leagueForm = Form(
     mapping(
     "id" -> ignored(-1l),
     "name" -> nonEmptyText(maxLength=255),
     "location" -> text,
     "description" -> text,
     "active" -> boolean
     )(League.apply)(League.unapply)
  )

  def leagues = Action {
    Ok(views.html.leagues(League.all(), leagueForm))
  }

  def newLeague =  Action { implicit request =>
    leagueForm.bindFromRequest.fold(
      errors => BadRequest(views.html.leagues(League.all(), leagueForm)),
      league => {
        League.create(league.name, league.location, league.description)
        Redirect(routes.Application.leagues)
      }
    )
  }
}