// ---------------------------------------------------------------------------
// Basic settings

name := "argot"

organization := "org.clapper"

version := "1.0.2"

licenses := Seq("BSD" -> url("http://software.clapper.org/argot/license.html"))

homepage := Some(url("http://software.clapper.org/argot/"))

description := "A command-line option and parameter parser"

scalaVersion := "2.11.0"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.11.0", "2.11.1")

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("argument parser", "command line", "parameters")

(description in LsKeys.lsync) <<= description(d => d)

// ---------------------------------------------------------------------------
// Dependendencies

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.1.7" % "test",
    "org.clapper" %% "grizzled-scala" % "1.2"
)

// ---------------------------------------------------------------------------
// Publishing criteria

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

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
