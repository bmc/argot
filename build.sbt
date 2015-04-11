// ---------------------------------------------------------------------------
// Basic settings

name := "argot"

organization := "org.clapper"

version := "1.0.4"

licenses := Seq("BSD" -> url("http://software.clapper.org/argot/license.html"))

homepage := Some(url("http://software.clapper.org/argot/"))

description := "A command-line option and parameter parser"

scalaVersion := "2.10.4"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.10.4", "2.11.1")

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("argument parser", "command line", "parameters")

(description in LsKeys.lsync) <<= description(d => d)

bintraySettings

bintray.Keys.packageLabels in bintray.Keys.bintray := (
  LsKeys.tags in LsKeys.lsync
).value

// ---------------------------------------------------------------------------
// Dependendencies

libraryDependencies ++= Seq(
  "org.clapper" %% "grizzled-scala" % "1.2",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test"
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
