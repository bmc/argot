import sbt._

class ArgotProject(info: ProjectInfo)
extends DefaultProject(info) with posterous.Publish
{
    /* ---------------------------------------------------------------------- *\
                         Compiler and SBT Options
    \* ---------------------------------------------------------------------- */

    override def compileOptions = Unchecked :: super.compileOptions.toList
    override def parallelExecution = true // why not?

    override def documentOptions = 
        documentTitle(projectName + " " + projectVersion) ::
        super.documentOptions.toList

    /* ---------------------------------------------------------------------- *\
                             Various settings
    \* ---------------------------------------------------------------------- */

    /* ---------------------------------------------------------------------- *\
                       Managed External Dependencies
    \* ---------------------------------------------------------------------- */

    val newReleaseToolsRepository = "Scala Tools Repository" at
        "http://nexus.scala-tools.org/content/repositories/snapshots/"

    val grizzled = "org.clapper" %% "grizzled-scala" % "1.0.6"

    val (scalatestArtifact, scalatestVersion) = buildScalaVersion match
    {
        case "2.8.0"           => ("scalatest", "1.3")
        case "2.8.1"           => ("scalatest", "1.3")
        case "2.9.0"           => ("scalatest_2.9.0", "1.4.1")
        case n                 => error("Unsupported Scala version " + n)
    }

    val scalatest = "org.scalatest" % scalatestArtifact % scalatestVersion % "test"

    /* ---------------------------------------------------------------------- *\
                                Publishing
    \* ---------------------------------------------------------------------- */

    lazy val publishTo = "Scala Tools Nexus" at
        "http://nexus.scala-tools.org/content/repositories/releases/"
    Credentials(Path.userHome / "src" / "mystuff" / "scala" /
                "nexus.scala-tools.org.properties", log)

    override def managedStyle = ManagedStyle.Maven

    /* ---------------------------------------------------------------------- *\
                                   Tasks
    \* ---------------------------------------------------------------------- */

    /* ---------------------------------------------------------------------- *\
                              Private Methods
    \* ---------------------------------------------------------------------- */
}
