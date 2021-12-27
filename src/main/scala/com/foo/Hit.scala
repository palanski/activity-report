package com.foo

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import scala.util.Try
import scala.util.matching.Regex

import cats.implicits._
import fs2.io.file.{Files, Path}
import fs2.{Stream, text}

package object hit {

  private[foo] def readHits[F[_] : Files](parse: String => Option[Hit])(file: Path): Stream[F, Hit] =
    Files[F].readAll(file)
      .through(text.utf8.decode)
      .through(text.lines)
      .map(parse)
      .mapFilter(identity)
}

package hit {

  /**
   * An single user's request.
   *
   * @param userId
   * @param time
   */
  private[foo] final case class Hit(userId: UserId, time: Msec)

  private[foo] object Hit {

    private[this] val hitPattern: Regex =
      """^(\d{1,3}\.){3}\d{1,3}\s-\s-\s(.{26})\s"([^/]+/)([^/]{8}/){2}([^/]{8})(/[^/]+)* HTTP.*""".r

    private[this] val dateFormatter: DateTimeFormatter =
      DateTimeFormatter.ofPattern("dd/MMM/yyyy:kk:mm:ss x", Locale.US)

    def parse(s: String): Option[Hit] =
      Try(s match {
        case hitPattern(_, date, _, _, userId, _) =>
          Hit(userId, ZonedDateTime.parse(date, dateFormatter).toInstant.toEpochMilli)
      }).toOption
  }
}
