import sbt.Keys._
import sbt._

object Build extends sbt.Build {
  import Settings._

  lazy val root = Project(
    id = "hoecoga-play-api",
    base = file("."),
    settings = defaultSettings ++ testSettings ++ playSettings)
}

object Settings {
  val defaultSettings = Seq(scalaVersion := "2.11.7")

  val testSettings = Seq(libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test")

  val playSettings = Seq(libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % play.core.PlayVersion.current,
    "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "test"))
}
