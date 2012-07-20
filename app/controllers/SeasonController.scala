package controllers

import play.api.mvc.{Action, Controller}
import models.{Team, Week, Season}

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/7/12
 * Time: 10:12 PM
 * To change this template use File | Settings | File Templates.
 */

object SeasonController extends Controller {
  def schedule(seasonId: Long) = Action {
    val season: Option[Season] = Season.findById(seasonId)
    if (season == None) {
      NoContent
    } else {
      Ok(views.html.schedule(season.get,
        Team.findByLeagueId(season.get.leagueId.get, false).groupBy(_.id.get).mapValues(_.head),
        Week.findBySeasonId(seasonId)))
    }
  }

  def standings(seasonId: Long) = Action {
    Ok("Standings Here - TODO")
  }

  def teamSchedule(seasonId: Long, teamId: Long) = TODO

}
