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
import anorm.Id
import html.league

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 8/6/12
 * Time: 10:40 PM
 */

case class ScoredGame(gameId: Long, teams: Option[(String, String)], scores: Seq[Option[(Int, Int)]])

case class WeekOfGames(games: Seq[ScoredGame])

object WeekController extends Controller {

  val weekOfGamesForm: Form[WeekOfGames] = Form(
    mapping(
      "games" -> seq(
        mapping(
          "gameId" -> longNumber,
          "teams" -> optional(tuple("team1" -> text, "team2" -> text)),
          "scores" -> seq(
            optional(tuple("team1" -> number, "team2" -> number))
          )
        )(ScoredGame.apply)(ScoredGame.unapply)
      )
    )(WeekOfGames.apply)(WeekOfGames.unapply)
  )

  def submitScores(weekId: Long) = Action {
    implicit request =>
      weekOfGamesForm.bindFromRequest.fold(
        errors => BadRequest(Html("Some error: " + errors.toString())),
        weekOfGames => {
          weekOfGames.games foreach( scoredGame => {
            for(i <- 0 until scoredGame.scores.length; score = scoredGame.scores(i))
              if(score isDefined) Game.scoreSet(scoredGame.gameId, (i + 1).toShort, score.get._1.toShort, score.get._2.toShort)
          })
          Ok("Saved scores for week " + weekId)
        }
      )
  }

  def editWeekScores(weekId: Long) = Action {
    Ok(getWeekOfGamesForm(weekId))
  }

  def getWeekOfGamesForm(weekId: Long): Html = {
    val teamMap = Team.mapById(Team.findByWeekId(weekId, false))

    val weekOfGames = WeekOfGames(Game.findByWeekId(weekId).map {
      g: Game =>
        ScoredGame(g.id.get,
          Some((teamMap(g.team1Id).name, teamMap(g.team2Id).name)),
          g match {
            case scheduled: ScheduledGame => List.fill(scheduled.numSets)(None);
            case completed: CompletedGame => completed.setScores.map {
              s: String => {
                val scores = s.split("-");
                (Some(scores(0).toInt, scores(1).toInt))
              }
            };
          })
    })

    if (weekOfGames.games.isEmpty) Html("<div>No games scheduled.</div>") else html.forms.editscores(weekOfGamesForm.fill(weekOfGames), weekId)
  }
}
