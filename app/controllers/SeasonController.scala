package controllers

import play.api.mvc.{Action, Controller}

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/7/12
 * Time: 10:12 PM
 * To change this template use File | Settings | File Templates.
 */

object SeasonController extends Controller {
  def schedule(seasonId: Long) = Action {
    Ok("Complete Schedule Here")
  }

  def standings(seasonId: Long) = Action {
    Ok("Standings Here")
  }

  def teamSchedule(seasonId: Long, teamId:Long) = TODO

}
