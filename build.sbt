name := """sample scala room"""
organization := "com.ilanpillemer"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += ws

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "3.0.0" % Test
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.ilanpillemer.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.ilanpillemer.binders._"
