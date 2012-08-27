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
import controllers.Application
import views.html.week

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 6/30/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */

case class Set(num: Int, gameId: Long, team1Score: Option[Int], team2Score: Option[Int])

object Set {
  //  val setParser = {
  //    int("set.num") ~ long("set.game_id") ~ get[Option[Int]]("set.team1_score") ~ get[Option[Int]]("set.team2_score") map {
  //      case num ~ game_id ~ team1_score ~ team2_score => new Set(num, game_id, team1_score, team2_score)
  //    }
  //  }

  def create(num: Short, gameId: Long, team1Score: Option[Short] = None, team2Score: Option[Short] = None): Option[Long] = DB.withConnection {
    implicit c =>
      SQL("insert into set (num, game_id, team1_score, team2_score) " +
        "values ({num}, {game_id}, {team1_score}, {team2_score})").
        on('num -> num, 'game_id -> gameId, 'team1_score -> team1Score, 'team2_score -> team2Score)
        .executeInsert()
  }
}

abstract class Game() {
  def id: Pk[Long]
  def team1Id: Long
  def team2Id: Long
}

case class ScheduledGame(id: Pk[Long] = NotAssigned, weekId: Long, startTime: LocalTime, court: Int, team1Id: Long, team2Id: Long,
                         numSets: Int) extends Game

case class CompletedGame(id: Pk[Long], weekId: Long, team1Id: Long, team2Id: Long, team1Wins: Int, team2Wins: Int, setScores: List[String]) extends Game

object Game {
  val gameRowParser = {
    get[Pk[Long]]("game.id") ~ long("game.week_id") ~ date("game.start_time") ~ int("game.court") ~ long("game.team1_id") ~ long("game.team2_id") ~ int("game.num_sets")
  }

  val simpleParser = {
    gameRowParser map {
      case id ~ week_id ~ start_time ~ court ~ team1_id ~ team2_id ~ num_sets
      => new ScheduledGame(id, week_id, LocalTime.fromDateFields(start_time), court, team1_id, team2_id, num_sets)
    }
  }

  val gamesAndSetsParser = {
    (gameRowParser ~ int("set.num") ~ long("set.game_id") ~ get[Option[Int]]("set.team1_score") ~ get[Option[Int]]("set.team2_score")) map {
      case id ~ week_id ~ start_time ~ court ~ team1_id ~ team2_id ~ _ ~ _ ~ _ ~ Some(t1score) ~ Some(t2score)
      => new CompletedGame(id, week_id, team1_id, team2_id,
        if (t1score > t2score) 1 else 0, if (t2score > t1score) 1 else 0, List(t1score + "-" + t2score))
      case id ~ week_id ~ start_time ~ court ~ team1_id ~ team2_id ~ num_sets ~ _ ~ _ ~ _ ~ _
      => new ScheduledGame(id, week_id, LocalTime.fromDateFields(start_time), court, team1_id, team2_id, num_sets)
    }
  }

  def create(weekId: Long, startTime: LocalTime, court: Int, team1Id: Long, team2Id: Long,
             numSets: Int = 3, playoff: Boolean = false): Long = DB.withConnection {
    implicit c =>
      val matchId: Long = SQL(
        """
        insert into game (week_id, start_time, court, team1_id, team2_id, num_sets)
        values ({week_id}, {start_time}, {court}, {team1_id}, {team2_id}, {num_sets})
        """)
        .on('week_id -> weekId, 'start_time -> startTime.toDateTimeToday.toDate, 'court -> court,
        'team1_id -> team1Id, 'team2_id -> team2Id, 'num_sets -> numSets)
        .executeInsert().get

      for (i <- 1 to numSets) {
        Set.create(i.toShort, matchId)
      }

      matchId
  }

  def processGames(rows: List[Game]): List[Game] = {
    rows match {
      case Nil => Nil
      case List(game: ScheduledGame, _*) => {
        val (_, remainingRows) = rows span (_.id == game.id)
        game :: processGames(remainingRows)
      }
      case List(game: CompletedGame, _*) => {
        val (thisGameRows, remainingRows) = rows span (_.id == game.id)
        // we know if the first one is a CompletedGame, the others in the result set will also be (until we get to the next game)
        // asInstanceOf here seems to avoid the compiler's scary "unchecked since it is eliminated by erasure" warning.
        thisGameRows.asInstanceOf[List[CompletedGame]].reduceLeft((g1, g2) => g1.copy(team1Wins = g1.team1Wins + g2.team1Wins, team2Wins = g1.team2Wins + g2.team2Wins, setScores = g1.setScores ++ g2.setScores)) :: processGames(remainingRows)
      }
    }
  }

  //  def findById(gameId: Long) : Option[Game] = DB.withConnection {
  //    implicit c =>
  //      SQL(
  //        """
  //            SELECT * FROM game where game.id = {id}
  //        """)
  //        .on('id -> gameId).as(simpleParser.singleOpt)
  //  }


  def findByWeekId(weekId: Long): List[Game] = DB.withConnection {
    implicit c =>
      processGames(SQL(
        """
           SELECT * FROM game
           LEFT OUTER JOIN set ON set.game_id = game.id
           WHERE game.week_id = {week_id}
           ORDER BY game.week_id, game.start_time, game.court, set.num
        """
      )
        .on('week_id -> weekId)
        .as(gamesAndSetsParser *)
      )
  }

  def findCompletedGamesForSeason(seasonId: Long): scala.List[CompletedGame] = DB.withConnection {
    implicit c =>
      processGames(SQL(
        """
           SELECT game.*, set.* FROM game
           LEFT OUTER JOIN set ON set.game_id = game.id
           JOIN week ON game.week_id = week.id
           WHERE week.season_id = {season_id}
            AND set.team1_score IS NOT NULL and set.team2_score IS NOT NULL
           ORDER BY game.week_id, game.start_time, game.court, set.num
        """
      )
        .on('season_id -> seasonId)
        .as(gamesAndSetsParser *)
      ).map {
        case g: CompletedGame => g
      }
  }


  def scoreSet(gameId: Long, setNum: Short, team1Score: Short, team2Score: Short) = DB.withConnection {
    implicit c =>
      SQL( """ update set
          set team1_score = {team1_score}, team2_score = {team2_score}
          where num = {num} and game_id = {game_id}
           """)
        .on('team1_score -> team1Score, 'team2_score -> team2Score, 'num -> setNum, 'game_id -> gameId)
        .executeUpdate()
  }
}

