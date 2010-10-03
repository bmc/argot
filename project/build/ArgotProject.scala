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

    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val grizzled = "org.clapper" %% "grizzled-scala" % "1.0.1"

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
