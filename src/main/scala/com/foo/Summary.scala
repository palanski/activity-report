package com.foo

import java.lang.Math.{max, min}

import cats.Semigroup
import cats.implicits._
import fs2.Stream

import com.foo.session.Session

package object summary {

  private[foo] def toSummaries[F[_] : λ[A[_] => fs2.Compiler[A, A]]](
    sessions: Stream[F, (UserId, Session)]
  ): F[Map[UserId, Summary]] = {
    sessions.compile.fold(Map.empty[UserId, Summary]) {
      case (report, (userId, session)) =>
        report |+| Map(userId -> Summary(session.hits, 1, session.duration, session.duration))
    }
  }
}

package summary {

  /**
   * A summary for user's sessions.
   *
   * @param hits request count
   * @param sessions
   * @param shortestSession
   * @param longestSession
   */
  private[foo] final case class Summary(hits: Int, sessions: Int, shortestSession: Msec, longestSession: Msec)

  private[foo] object Summary {

    implicit val summarySemigroup: Semigroup[Summary] = Semigroup.instance {
      case (a, b) => Summary(
        a.hits + b.hits,
        a.sessions + b.sessions,
        min(a.shortestSession, b.shortestSession),
        max(a.longestSession, b.longestSession)
      )
    }
  }
}