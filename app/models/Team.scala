package models

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */

import org.joda.time.{LocalTime, LocalDate, DateTime, DateMidnight}
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */

case class Team(id: Pk[Long] = NotAssigned, name: String, captainName: String, captainEmail: String)

object Team {
  val team = {
    get[Pk[Long]]("team.id") ~
      str("team.name") ~
      str("team.captain_name") ~
      str("team.captain_email") map {
      case id ~ name ~ captain_name ~ captain_email => new Team(id, name, captain_name, captain_email)
    }
  }

  val teamNoEmail = {
    get[Pk[Long]]("team.id") ~
      str("team.name") ~
      str("team.captain_name") ~
      str("team.captain_email") map {
      case id ~ name ~ captain_name ~ _ => new Team(id, name, captain_name, "XXX@XXX")
    }
  }

  def create(name: String, captainName: String, captainEmail: String): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into team (name, captain_name, captain_email) " +
        "values ({name}, {captain_name}, {captain_email})").
        on('name -> name, 'captain_name -> captainName, 'captain_email -> captainEmail)
        .executeInsert()
  }

  def addToLeague(teamId: Long, leagueId: Long) = DB.withConnection {
    implicit c =>
      SQL("insert into team2league (league_id, team_id) values ({league_id}, {team_id})")
        .on('league_id -> leagueId, 'team_id -> teamId)
        .executeInsert()
  }

  def findByLeagueId(leagueId: Long, includeEmail: Boolean): List[Team] = DB.withConnection {
    implicit c =>
      SQL( """
          select team.* from team
          join team2league t2l on t2l.league_id = {league_id}
          where team.id = t2l.team_id
           """)
        .on('league_id -> leagueId)
        .as((if (includeEmail) team else teamNoEmail) *)
  }

  def mapById(teams: List[Team]): Map[Long, Team] = teams.groupBy(_.id.get).mapValues(_.head)
}
