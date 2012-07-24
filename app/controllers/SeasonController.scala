package controllers

import play.api.mvc.{Action, Controller}
import models._
import scala.Some

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/7/12
 * Time: 10:12 PM
 */

case class WinLossRecord(wins: Int, losses: Int) {
  def +(that: WinLossRecord) = this.copy(wins = wins + that.wins, losses = losses + that.losses)
}

case class StandingsLine(place: Int, teamId: Long, wins: Int, losses: Int) {
  def % : Float = wins.toFloat / (wins.toFloat + losses.toFloat)
}

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

  def teamSchedule(seasonId: Long, teamId: Long) = Action {
    Season.findById(seasonId) match {
      case None => NoContent
      case Some(season) => {
        val teamMap: Map[Long, Team] = Team.mapById(Team.findByLeagueId(season.leagueId.get, false))
        Ok(views.html.schedule(
          teamMap(teamId).name + " Schedule",
          teamMap(teamId).name + " Schedule (" + season.leagueName + ")",
          teamMap,
          Week.findByTeamId(seasonId, teamId)))
      }
    }
  }

  def standings(seasonId: Long) = Action {
    Ok("Standings Here - TODO")
  }

  def calculateStandings(seasonId: Long): Iterable[StandingsLine] = {
    Season.findById(seasonId) match {
      case None => List.empty
      case Some(season) => {
        val winloss = (Map[Long, WinLossRecord]() /: Game.findCompletedGamesForSeason(season.id.get)) {
          (m, g) => m + (g.team1Id -> (WinLossRecord(g.team1Wins, g.team2Wins) + m.getOrElse(g.team1Id, WinLossRecord(0, 0)))) + (g.team2Id -> (WinLossRecord(g.team2Wins, g.team1Wins) + m.getOrElse(g.team2Id, WinLossRecord(0, 0))))
        }

        val sortedStandings = (for ((k, v) <- winloss) yield StandingsLine(0, k, v.wins, v.losses)).toSeq.sortBy(_.%)
        for (i <- (0 until sortedStandings.length).reverse; p = sortedStandings.length - i) yield sortedStandings(i).copy(place = p)

      }
    }
  }
}
