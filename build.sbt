name := """Age of Scala"""

version := "0.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe" % "config" % "1.2.1",
  "com.github.nscala-time" %% "nscala-time" % "2.12.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.lambdaworks" %% "jacks" % "2.3.3",
  "com.typesafe.play" % "play_2.11" % "2.5.3",
  "org.scalafx" %% "scalafx" % "8.0.102-R11",
  "junit" % "junit" % "4.4" % "test",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.specs2" %% "specs2-core" % "3.8.6" % "test",
  "org.scala-lang" % "scala-swing" % "2.11.0-M7"
)

scalacOptions in Test ++= Seq(
	"-Yrangepos"
)

parallelExecution in Test := false

fork in run := false