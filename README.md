Argot is a command-line parser library for [Scala][], supporting:

* single-value and multi-value options
* single-value and multi-value parameters
* flag and non-flag options
* GNU-style long options, i.e., "--option")
* POSIX-style short options, i.e., single "-" lead-in, with option
  grouping (e.g., "`tar -xcf foo.tgz`")
* automatic parameter conversion (i.e., values with non-string types,
  with automatic conversion)
* the ability to supply your own conversion functions

For more information, see the [Argot home page][].

**NOTE**: The `master` branch supports only Scala 2.10 and later. See
the [`pre-scala-2.10`](https://github.com/bmc/argot/tree/pre-scala-2.10)
branch for versions of Scala prior to 2.10.

[Scala]: http://www.scala-lang.org/
[Argot home page]: http://software.clapper.org/argot/

