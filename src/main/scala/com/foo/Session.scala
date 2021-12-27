package com.foo

import java.time.Duration
import java.time.temporal.ChronoUnit.MINUTES

import fs2.{Chunk, Pipe, Pull, Stream}

import com.foo.hit.Hit

package object session {

  private type State = Map[UserId, Session]

  private implicit class StateOps(val state: State) extends AnyVal {

    def startSession(userId: UserId, hit: Msec): State =
      state.updated(userId, Session(1, hit, hit))

    def updateSession(userId: UserId, hits: Int, firstHit: Msec, lastHit: Msec): State =
      state.updated(userId, Session(hits, firstHit, lastHit))
  }

  /**
   * Groups hits into sessions. The order of sessions is undefined. The only guarantee exists is
   * that all the hits will be grouped into sessions properly and if all the sessions combined
   * together, total number of hits from sessions will be equal to the number of hits processed.
   *
   * @tparam F
   * @return
   */
  private[foo] def toSessions[F[_]]: Pipe[F, Hit, (UserId, Session)] = {

    def combine(hits: Chunk[Hit], state: State): (Vector[(UserId, Session)], State) =
      hits.foldLeft((Vector.empty[(UserId, Session)], state)) {
        case ((sessions, state), Hit(userId, hit)) => state.get(userId) match {
          case Some(s) if hit - s.lastHit < Session.ExpPeriod =>
            (sessions, state.updateSession(userId, s.hits + 1, s.firstHit, hit))
          case Some(s) => (sessions.appended(userId -> s), state.startSession(userId, hit))
          case None => (sessions, state.startSession(userId, hit))
        }
      }

    def go(hits: Stream[F, Hit], state: State): Pull[F, (UserId, Session), Unit] = {
      hits.pull.uncons.flatMap {
        case None => Pull.output(Chunk.iterable(state)) >> Pull.done
        case Some((h, t)) =>
          val (sessions, updatedState) = combine(h, state)
          Pull.output(Chunk.vector(sessions)) >> go(t, updatedState)
      }
    }

    hits => go(hits, Map.empty).stream
  }
}

package session {

  /**
   * Represents a series of user's hits following each other within the session's expiration period.
   *
   * @param hits hit count
   * @param firstHit
   * @param lastHit
   */
  private[foo] final case class Session(hits: Int, firstHit: Msec, lastHit: Msec) {

    def duration: Msec = lastHit - firstHit
  }

  private[foo] object Session {

    val ExpPeriod: Msec = Duration.of(10, MINUTES).toMillis
  }
}
