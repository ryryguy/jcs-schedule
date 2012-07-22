package controllers

import play.api.mvc.{Action, Controller}
import models.{Team, Week, Season}
import com.sun.imageio.plugins.jpeg.JPEG.JCS

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
      Ok(views.html.schedule(season.get.leagueName + " Schedule",
        season.get.leagueName + " Schedule",
        Team.mapById(Team.findByLeagueId(season.get.leagueId.get, false)),
        Week.findBySeasonId(seasonId)))
    }
  }

  def standings(seasonId: Long) = Action {
    Ok("Standings Here - TODO")
  }

  def teamSchedule(seasonId: Long, teamId: Long) = Action {
    val season: Option[Season] = Season.findById(seasonId)
    if (season == None) {
      NoContent
    } else {
      val teamMap: Map[Long, Team] = Team.mapById(Team.findByLeagueId(season.get.leagueId.get, false))
      Ok(views.html.schedule(
        teamMap(teamId).name + " Schedule",
        teamMap(teamId).name + " Schedule (" + season.get.leagueName + ")",
        teamMap,
        Week.findByTeamId(seasonId, teamId)))
    }
  }
}
