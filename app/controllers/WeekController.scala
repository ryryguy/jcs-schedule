package controllers

import play.api.mvc.{Action, Controller}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.templates.Html
import models._
import org.joda.time.{DateTimeZone, DateMidnight}
import models.ScheduledGame

import views._

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 8/6/12
 * Time: 10:40 PM
 */

case class ScoredGame(gameId: Long, teams: (String, String), scores: Seq[Option[(Int, Int)]])

case class WeekOfGames(games: Seq[ScoredGame])

object WeekController extends Controller {

  val weekOfGamesForm: Form[WeekOfGames] = Form(
    mapping(
      "games" -> seq(
        mapping(
          "gameId" -> longNumber,
          "teams" -> tuple("team1" -> text, "team2" -> text),
          "scores" -> seq(
            optional(tuple("team1" -> number, "team2" -> number))
          )
        )(ScoredGame.apply)(ScoredGame.unapply)
      )
    )(WeekOfGames.apply)(WeekOfGames.unapply)
  )

  def submitScores(weekId: Long) = TODO

  def editWeekScores(weekId: Long) = Action {
    Ok(getWeekOfGamesForm(weekId))
  }

  def getWeekOfGamesForm(weekId: Long): Html = {
    val teamMap = Team.mapById(Team.findByWeekId(weekId, false))

    val weekOfGames = WeekOfGames(Game.findByWeekId(weekId).map {
      g: Game =>
        ScoredGame(g.id.get,
          (teamMap(g.team1Id).name, teamMap(g.team2Id).name),
          g match {
            case scheduled: ScheduledGame => List.fill(scheduled.numSets)(None);
            case completed: CompletedGame => completed.setScores.map {
              s: String => {
                val scores = s.split("-"); (Some(scores(0).toInt, scores(1).toInt))
              }
            };
          })
    })

    html.forms.editscores(weekOfGamesForm.fill(weekOfGames), weekId)
  }
}
