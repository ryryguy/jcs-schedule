package models

import org.joda.time.{LocalDate, DateMidnight}
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/27/12
 * Time: 8:51 PM
 * To change this template use File | Settings | File Templates.
 */

class Season(val id:Long = -1, val leagueName:String, val start_date:LocalDate, val completed:Boolean,
             val weeksRegular:Int, val weeksPlayoffs:Int, val byes:Int, val doubleheaders:Int )

abstract class LeagueSeason

case class CurrentSeason(s:Season) extends LeagueSeason
//  extends Season(s.id, s.leagueName, s.start_date, s.completed, s.weeksRegular, s.weeksPlayoffs, s.byes, s.doubleheaders)

case class CompletedSeason(s:Season) extends LeagueSeason
//  extends Season(s.id, s.leagueName, s.start_date, s.completed, s.weeksRegular, s.weeksPlayoffs, s.byes, s.doubleheaders)

case class NextSeason(s:Season) extends LeagueSeason
//  extends Season(s.id, s.leagueName, s.start_date, s.completed, s.weeksRegular, s.weeksPlayoffs, s.byes, s.doubleheaders)


object Season {
  val season = {
      long("id") ~
        date("start_date") ~
        bool("completed") ~
        int("weeks_Regular") ~
        int("weeks_Playoffs")~
        int("byes") ~
        int("doubleheaders") ~
        str("league_name") map {
    case id ~ start_date ~ completed ~ weeks_Regular ~ weeks_Playoffs ~ byes ~ doubleheaders ~ league_name
    => new Season(id, league_name, new LocalDate(start_date), completed, weeks_Regular, weeks_Playoffs, byes, doubleheaders)
    }
  }

  def create(leagueId:Long, start_date:String,
             weeksRegular:Int, weeksPlayoffs:Int, byes:Int, doubleheaders:Int) : Option[Long] = DB.withConnection {
      implicit c =>
        SQL("insert into season (league_id, start_date, weeks_regular, weeks_playoffs, byes, doubleheaders) " +
          "values ({league_id}, {start_date}, {weeksRegular}, {weeksPlayoffs}, {byes}, {doubleheaders})").
          on('league_id -> leagueId, 'start_date -> start_date, 'weeksRegular -> weeksRegular, 'weeksPlayoffs -> weeksPlayoffs, 'byes -> byes, 'doubleheaders -> doubleheaders)
          .executeInsert()
  }

  def getForLeague(leagueId:Long) : List[LeagueSeason] = {
    val seasons = DB.withConnection {
      implicit c =>
        SQL(
          """
          select s.*, l.league_name from season s
          join league l on s.league_id = l.id
          """).
          on('leagueId -> leagueId).as(season *)
    }

    for( s <- seasons ) yield {
      if (!s.completed && s.start_date.isAfter(LocalDate.now())) CurrentSeason(s)
      else
      if(!s.completed) NextSeason(s)
      else CompletedSeason(s)
    }
  }

  def current(leagueId:Long) = DB.withConnection {
      implicit c =>
        SQL(
          """
          select s.*, l.league_name from season s
          join league l on s.league_id = l.id
          where league_id = {leagueId} and completed = false and start_date <= {today}
          """).
          on('leagueId -> leagueId, 'today -> new DateMidnight().toString("yyyy-MM-dd")).
          as(season.singleOpt).getOrElse(null)
  }

  def next(leagueId:Long) = DB.withConnection {
      implicit c =>
        SQL(
          """
          select s.*, l.league_name from season s
            join league l on s.league_id = l.id
            where league_id = {leagueId} and completed = false and start_date > {today} order by start_date limit 1
          """).
            on('leagueId -> leagueId, 'today -> new DateMidnight().toString("yyyy-MM-dd")).
            as(season.singleOpt).getOrElse(null)
  }
}
