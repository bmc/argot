// ---------------------------------------------------------------------------
// Basic settings

name := "argot"

organization := "org.clapper"

version := "1.0.4"

licenses := Seq("BSD" -> url("http://software.clapper.org/argot/license.html"))

homepage := Some(url("http://software.clapper.org/argot/"))

description := "A command-line option and parameter parser"

scalaVersion := "2.13.1"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.10")

// ---------------------------------------------------------------------------
// Dependendencies

libraryDependencies ++= Seq(
  "org.clapper" %% "grizzled-scala" % "4.9.3",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

// ---------------------------------------------------------------------------
// Publishing criteria

// Don't set publishTo. The Bintray plugin does that automatically.

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:bmc/argot.git/</url>
    <connection>scm:git:git@github.com:bmc/argot.git</connection>
  </scm>
  <developers>
    <developer>
      <id>bmc</id>
      <name>Brian Clapper</name>
      <url>http://www.clapper.org/bmc</url>
    </developer>
  </developers>
)
