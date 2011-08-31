---
title: "Change Log: Argot, a command-line parser for Scala"
layout: default
---

Version 0.3.3:

* Now builds against Scala 2.9.0.1, as well as Scala 2.9.0, 2.8.1 and 2.8.0.
* Converted to build with [SBT][] 0.10.1

Version 0.3.1:

* Now builds against Scala 2.9.0, as well as Scala 2.8.0 and 2.8.1.
* Updated to version 1.4.1 of [ScalaTest][] for Scala 2.9.0. (Still uses
  ScalaTest 1.3, for Scala 2.8).
* Updated to use [SBT][] 0.7.7.
* Updated to version 1.0.6 of the [Grizzled Scala][] library.

[ScalaTest]: http://www.scalatest.org/
[SBT]: http://code.google.com/p/simple-build-tool/
[Grizzled Scala]: http://software.clapper.org/grizzled-scala/

Version 0.3:

* Renamed to *clap* (**C**ommand **L**ine **A**rgument **P**arser)

* Addressed [Issue #1][]:

  1. In the usage display, options are now sorted so that the POSIX-style
     single-character option names always precede any longer GNU-style
     synonyms.
  2. The `Argot` constructor now supports an `sortOptions` parameter, which
     defaults to `true`. If set to `true`, the options in the usage output
     are sorted lexically. If set to `false`, the options are displayed in
     the order they were specified in the code.

[Issue #1]: https://github.com/bmc/argot/issues#issue/1

Version 0.2:

* Upgraded [Grizzled Scala][] dependency to version 1.0.3.
* Now compiles against [Scala][] 2.8.1, as well as 2.8.0.

[Grizzled Scala]: http://bmc.github.com/grizzled-scala/
[Scala]: http://www.scala-lang.org/

Version 0.1:

* Initial version.