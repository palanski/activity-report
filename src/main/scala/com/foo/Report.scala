package com.foo

import java.time.Duration

import com.foo.summary.Summary

package object report {

  private[foo] def toReport(summaries: Map[UserId, Summary]): Report = Report(
    summaries.size,
    summaries.toList.sortBy { case (_, Summary(hits, _, _, _)) => hits }(Ordering[Int].reverse).take(5) // top 5
  )

  private implicit class MsecOps(val ms: Msec) extends AnyVal {

    def toMinutes: Long = Duration.ofMillis(ms).toMinutes
  }

  private[foo] def printReport(r: Report): Unit = {
    println(s"Total unique users: ${r.users}")

    if (r.users > 0) {
      println(s"id              # pages # sess  longest shortest")
    }

    r.summaries.foreach {
      case (userId, Summary(hits, sessions, shortestSession, longestSession)) =>
        print(s"${userId.padTo(16, ' ')}")
        print(s"${hits.toString.padTo(8, ' ')}")
        print(s"${sessions.toString.padTo(8, ' ')}")
        print(s"${longestSession.toMinutes.toString.padTo(8, ' ')}")
        print(s"${shortestSession.toMinutes.toString.padTo(8, ' ')}")
        println()
    }
  }
}

package report {

  private[foo] final case class Report(users: Int, summaries: List[(UserId, Summary)])
}