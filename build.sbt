ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

lazy val root = (project in file("."))
  .settings(
    name := "activity-report",
  )

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.7.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.1"
libraryDependencies ++= Seq("co.fs2" %% "fs2-core", "co.fs2" %% "fs2-io").map(_ % "3.2.0")

libraryDependencies += "org.specs2" %% "specs2-core" % "4.13.1" % "test"
