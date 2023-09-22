ThisBuild / version := "0.1.0-SNAPSHOT"
name := "ScalaFundamentals"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings()

libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4")
