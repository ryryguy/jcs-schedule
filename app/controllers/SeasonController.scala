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

case class WinLossRecord(teamId: Long, wins: Int, losses: Int) {
  def +(that: WinLossRecord) = this.copy(wins = wins + that.wins, losses = losses + that.losses)
}

case class StandingsLine(teamId: Long, wins: Int, losses: Int, htH: Option[Int] = None) {
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

  val standingsOrdering: Ordering[StandingsLine] = Ordering.by[StandingsLine, (Float, Option[Int])](sl => (1.0f - sl.%, sl.htH))

  def calculateHeadToHead(games: Seq[CompletedGame], winLossRecords: Seq[WinLossRecord]): Seq[StandingsLine] = {
    val standings = (for (wl <- winLossRecords) yield StandingsLine(wl.teamId, wl.wins, wl.losses)).groupBy(_.%).values.flatMap {
      seq: Seq[StandingsLine] =>
        seq match {
          case Seq(_) => seq
          case Seq(l1: StandingsLine, l2: StandingsLine) => {
            val headToHeadGames: Seq[CompletedGame] = games.filter((game: CompletedGame) => (game.team1Id == l1.teamId && game.team2Id == l2.teamId) || (game.team1Id == l2.teamId && game.team2Id == l1.teamId))
            if (headToHeadGames.isEmpty)
              Seq(l1.copy(htH = Some(-1)), l2.copy(htH = Some(-1)))
            else {
              val headToHeadWL = calculateWinLoss(headToHeadGames)
              if (headToHeadWL.head.wins == headToHeadWL.head.losses)
                Seq(l1.copy(htH = Some(1)), l2.copy(htH = Some(1)))
              else
              if (headToHeadWL.maxBy(_.wins).teamId == l1.teamId)
                Seq(l1.copy(htH = Some(1)), l2.copy(htH = Some(2)))
              else
                Seq(l1.copy(htH = Some(2)), l2.copy(htH = Some(1)))
            }
          }
          case Seq(_*) => seq map(_.copy(htH = Some(-1)))
        }
    }

    standings.toSeq.sorted(standingsOrdering)
  }

  def calculateWinLoss(games: Seq[CompletedGame]): Iterable[WinLossRecord] = {
    (Map[Long, WinLossRecord]() /: games) {
      (m, g) => m +
        (g.team1Id -> (WinLossRecord(g.team1Id, g.team1Wins, g.team2Wins) + m.getOrElse(g.team1Id, WinLossRecord(g.team1Id, 0, 0)))) +
        (g.team2Id -> (WinLossRecord(g.team2Id, g.team2Wins, g.team1Wins) + m.getOrElse(g.team2Id, WinLossRecord(g.team2Id, 0, 0))))
    }.values
  }

  def calculateStandings(seasonId: Long): Iterable[StandingsLine] = {
    Season.findById(seasonId) match {
      case None => List.empty
      case Some(season) => {
        val games: List[CompletedGame] = Game.findCompletedGamesForSeason(season.id.get)
        calculateHeadToHead(games, calculateWinLoss(games).toSeq)
      }
    }
  }
}
