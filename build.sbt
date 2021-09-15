name := "akka-streams-maze-challenge"

version := "0.1"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.16"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.5",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)
