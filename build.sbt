ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "chat4ops",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.11.33",
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % "1.11.33",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.11.33",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.8"
))

