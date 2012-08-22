package controllers

import play.api.mvc.{Action, Controller}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.templates.Html
import models.{Week, Season}
import org.joda.time.{DateTimeZone, DateMidnight}

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 8/6/12
 * Time: 10:40 PM
 */

case class ScoredGame(gameId: Long, scores: Seq[Option[(Int, Int)]])

case class WeekOfGames(weekid: Long, games: Seq[ScoredGame])

object WeekController extends Controller {

  val weekOfGamesForm: Form[WeekOfGames] = Form(
    mapping(
      "weekId" -> longNumber,
      "games" -> seq(
        mapping(
          "gameId" -> longNumber,
          "scores" -> seq(
            optional(tuple("team1" -> number, "team2" -> number))
          )
        )(ScoredGame.apply)(ScoredGame.unapply)
      )
    )(WeekOfGames.apply)(WeekOfGames.unapply)
  )

  def editWeekScores(weekId: Long) = Action {
    Ok(getWeekOfGamesForm(weekId))
  }

  def getWeekOfGamesForm(weekId:Long): Html = {
    Html("editWeekScores NYI for week: " + weekId)
  }

}
