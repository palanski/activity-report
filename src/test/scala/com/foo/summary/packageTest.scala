package com.foo.summary

import java.lang.Math.{max, min}

import cats._
import fs2.Stream
import org.specs2.mutable.Specification

import com.foo.UserId
import com.foo.session.Session

class packageTest extends Specification {

  "Summaries" should {

    "be empty if there are no sessions" in {
      val sessions = Stream[Id, (UserId, Session)]()
      toSummaries(sessions) must beEqualTo(Map.empty)
    }

    "be created from a single session" in {
      val userId = "foo"
      val session = Session(3, 200, 500)

      val sessions = Stream[Id, (UserId, Session)](userId -> session)
      toSummaries(sessions) must beEqualTo(
        Map(userId -> Summary(session.hits, 1, session.duration, session.duration)))
    }

    "be created from sessions going in any order" in {
      val userId = "foo"
      val session1 = Session(3, 200, 500)
      val session2 = Session(5, 800, 1300)
      val sessions = List(userId -> session1, userId -> session2)
      val sessionsReversed = sessions.reverse

      val summaries1: Map[UserId, Summary] = toSummaries(Stream.iterable[Id, (UserId, Session)](sessions))
      val summaries2: Map[UserId, Summary] = toSummaries(Stream.iterable[Id, (UserId, Session)](sessionsReversed))

      summaries1 must beEqualTo(Map(userId -> Summary(session1.hits + session2.hits, 2,
        min(session1.duration, session2.duration), max(session1.duration, session2.duration))))
      summaries1 must equalTo(summaries2)
    }
  }
}
