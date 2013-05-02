// ---------------------------------------------------------------------------
// Basic settings

name := "argot"

organization := "org.clapper"

version := "1.0.1"

licenses := Seq("BSD" -> url("http://software.clapper.org/argot/license.html"))

homepage := Some(url("http://software.clapper.org/argot/"))

description := "A command-line option and parameter parser"

scalaVersion := "2.10.0"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.10.0", "2.10.1")

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("argument parser", "command line", "parameters")

(description in LsKeys.lsync) <<= description(d => d)

// ---------------------------------------------------------------------------
// ScalaTest dependendency

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
    // Select ScalaTest version based on Scala version
    val scalatestVersionMap = Map(
      "2.10.0" -> ("scalatest_2.10.0", "2.0.M5"),
      "2.10.1" -> ("scalatest_2.10.0", "2.0.M5")
    )
    val (scalatestArtifact, scalatestVersion) = scalatestVersionMap.getOrElse(
        sv, error("Unsupported Scala version for ScalaTest: " + scalaVersion)
    )
    deps :+ "org.scalatest" % scalatestArtifact % scalatestVersion % "test"
}

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
  // ScalaTest still uses the (deprecated) scala.actors API.
  deps :+ "org.scala-lang" % "scala-actors" % sv % "test"
}

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper" %% "grizzled-scala" % "1.1.2"
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
