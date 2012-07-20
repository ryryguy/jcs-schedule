package models

import org.joda.time.{DateTime, LocalDate, DateMidnight}
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date
import controllers.Application

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/27/12
 * Time: 8:51 PM
 * To change this template use File | Settings | File Templates.
 */

class Season(val id: Pk[Long] = NotAssigned, val leagueName: String, val leagueId: Pk[Long], val start_date: LocalDate, val completed: Boolean,
             val weeksRegular: Int, val weeksPlayoffs: Int, val byes: Int, val doubleheaders: Int)

abstract class LeagueSeason {
  def s: Season
}

case class CurrentSeason(s: Season) extends LeagueSeason

case class CompletedSeason(s: Season) extends LeagueSeason

case class NextSeason(s: Season) extends LeagueSeason


object Season {
  val season = {
    get[Pk[Long]]("season.id") ~
      date("season.start_date") ~
      bool("season.completed") ~
      int("season.weeks_regular") ~
      int("season.weeks_playoffs") ~
      int("season.byes") ~
      int("season.doubleheaders") ~
      str("league.league_name") ~
      get[Pk[Long]]("league.id") map {
      case id ~ start_date ~ completed ~ weeks_Regular ~ weeks_Playoffs ~ byes ~ doubleheaders ~ league_name ~ league_id
      => new Season(id, league_name, league_id, new LocalDate(start_date), completed, weeks_Regular, weeks_Playoffs, byes, doubleheaders)
    }
  }

  def create(leagueId: Long, start_date: DateTime,
             weeksRegular: Short, weeksPlayoffs: Short, byes: Short, doubleheaders: Short): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into season (league_id, start_date, weeks_regular, weeks_playoffs, byes, doubleheaders) " +
        "values ({league_id}, {start_date}, {weeksRegular}, {weeksPlayoffs}, {byes}, {doubleheaders})").
        on('league_id -> leagueId, 'start_date -> start_date.toDate, 'weeksRegular -> weeksRegular, 'weeksPlayoffs -> weeksPlayoffs, 'byes -> byes, 'doubleheaders -> doubleheaders)
        .executeInsert()
  }

  private val seasonSelect = "select s.*, l.league_name, l.id from season s join league l on s.league_id = l.id where "

  private def seasonSelectWhere(where: String) = seasonSelect.concat(where)

  def findById(seasonId: Long): Option[Season] = DB.withConnection {
    implicit c =>
      SQL(seasonSelectWhere("s.id = {id}")).
        on('id -> seasonId).singleOpt(season)
  }

  def getForLeague(leagueId: Long): List[LeagueSeason] = {
    val seasons = DB.withConnection {
      implicit c =>
        SQL(seasonSelectWhere("s.league_id = {leagueId}")).
          on('leagueId -> leagueId).as(season *)
    }

    for (s <- seasons) yield {
      if (!s.completed && s.start_date.isBefore(LocalDate.now())) CurrentSeason(s)
      else
      if (!s.completed) NextSeason(s)
      else CompletedSeason(s)
    }
  }

  def current(leagueId: Long): Option[Season] = DB.withConnection {
    implicit c =>
      SQL(seasonSelectWhere("league_id = {leagueId} and completed = false and start_date <= {today}"))
        .on('leagueId -> leagueId, 'today -> new DateMidnight().toDate)
        .as(season.singleOpt)
  }

  def next(leagueId: Long): Option[Season] = DB.withConnection {
    implicit c =>
      SQL(seasonSelectWhere("league_id = {leagueId} and completed = false and start_date > {today} order by start_date limit 1"))
        .on('leagueId -> leagueId, 'today -> new DateMidnight().toDate)
        .as(season.singleOpt)
  }
}
