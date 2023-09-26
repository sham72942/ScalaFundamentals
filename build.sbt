ThisBuild / version := "0.1.0-SNAPSHOT"
name := "ScalaFundamentals"

ThisBuild / scalaVersion := "2.13.12"

lazy val akkaVersion = "2.8.4"
lazy val scalaTest = "3.1.0"

lazy val root = (project in file("."))
  .settings()

libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.7"
)

//Test dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTest % Test)
