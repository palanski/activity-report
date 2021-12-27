package com.foo

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.io.file.{Files, NoSuchFileException, NotDirectoryException, Path}
import cats.implicits._

import com.foo.hit._
import com.foo.session._
import com.foo.summary._
import com.foo.report._

object ActivityReport extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = args.headOption match {
    case Some(logDir) =>
      val sessions = listFiles(Path(logDir))
        .flatMap(readHits[IO](Hit.parse))
        .through(toSessions)

      toSummaries(sessions)
        .map(toReport)
        .flatMap(r => IO(printReport(r)))
        .onError {
          case ReportableError(msg) => IO(logErr(msg))
          case _ => IO.unit // exit code indicates an error
        }.redeem(_ => ExitCode.Error, _ => ExitCode.Success)
    case None => IO(logErr(s"Usage: Reporter <log dir>")).as(ExitCode.Error)
  }

  private def logErr(msg: String): Unit = System.err.println(msg)

  private def listFiles(dir: Path): Stream[IO, Path] =
    Files[IO].list(dir).adaptErr {
      case err: NoSuchFileException => ReportableError(s"The dir '${err.getMessage}' does not exist.")
      case err: NotDirectoryException => ReportableError(s"'${err.getMessage}' is not a directory.")
      case err => err
    }
}
