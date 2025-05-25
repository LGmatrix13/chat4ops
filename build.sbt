ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "chat4ops",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.5.3",
      "com.typesafe.akka" %% "akka-stream" % "2.8.8",
      "io.circe" %% "circe-core" % "0.14.13",
      "io.circe" %% "circe-parser" % "0.14.13",
      "io.circe" %% "circe-generic" % "0.14.13",
      "ch.qos.logback" % "logback-classic" % "1.5.18"
    )
  )

