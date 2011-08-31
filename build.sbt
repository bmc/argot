// ---------------------------------------------------------------------------
// Basic settings

name := "argot"

organization := "org.clapper"

version := "0.3.5"

scalaVersion := "2.8.1"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked")

crossScalaVersions := Seq("2.9.1", "2.9.0-1", "2.9.0", "2.8.1", "2.8.0")

// ---------------------------------------------------------------------------
// ScalaTest dependendency

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
    // Select ScalaTest version based on Scala version
    val scalatestVersionMap = Map("2.8.0"   -> ("scalatest", "1.3"),
                                  "2.8.1"   -> ("scalatest_2.8.1", "1.5.1"),
                                  "2.9.0"   -> ("scalatest_2.9.0", "1.6.1"),
                                  "2.9.0-1" -> ("scalatest_2.9.0-1", "1.6.1"),
                                  "2.9.1"   -> ("scalatest_2.9.0-1", "1.6.1"))
    val (scalatestArtifact, scalatestVersion) = scalatestVersionMap.getOrElse(
        sv, error("Unsupported Scala version: " + scalaVersion)
    )
    deps :+ "org.scalatest" % scalatestArtifact % scalatestVersion % "test"
}

fork in Test := true

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper" %% "grizzled-scala" % "1.0.8"
)

// ---------------------------------------------------------------------------
// Publishing criteria

publishTo <<= version {(v: String) =>
    val nexus = "http://nexus.scala-tools.org/content/repositories/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "snapshots/") 
    else                             Some("releases"  at nexus + "releases/")
}

publishMavenStyle := true

credentials += Credentials(Path.userHome / "src" / "mystuff" / "scala" /
                           "nexus.scala-tools.org.properties")
