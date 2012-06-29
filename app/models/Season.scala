package models

import org.joda.time.DateMidnight
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

case class Season(id:Long = -1, leagueName:String, start_date:DateMidnight, completed:Boolean,
                  weeksRegular:Int, weeksPlayoffs:Int, byes:Int, doubleheaders:Int )

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
    => Season(id, league_name, new DateMidnight(start_date), completed, weeks_Regular, weeks_Playoffs, byes, doubleheaders)
    }
  }

  def create(leagueId:Long, start_date:String,
             weeksRegular:Int, weeksPlayoffs:Int, byes:Int, doubleheaders:Int) {
    DB.withConnection {
      implicit c =>
        SQL("insert into season (league_id, start_date, weeks_regular, weeks_playoffs, byes, doubleheaders) " +
          "values ({league_id}, {start_date}, {weeksRegular}, {weeksPlayoffs}, {byes}, {doubleheaders})").
          on('league_id -> leagueId, 'start_date -> start_date, 'weeksRegular -> weeksRegular, 'weeksPlayoffs -> weeksPlayoffs, 'byes -> byes, 'doubleheaders -> doubleheaders)
          .executeUpdate()
    }
  }

  def current(leagueId:Long) = {
    DB.withConnection {
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
  }

  def next(leagueId:Long) = {
    DB.withConnection {
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
}
