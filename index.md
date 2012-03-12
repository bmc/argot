---
title: Argot, a command-line parser for Scala
layout: withTOC
---

# Introduction

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

# Installation

Argot is published to the `oss.sonatype.org` repository;  Sonatype
automatically pushes the artifacts to the  [Maven central repository][]. You
can get download the Grizzled Scala library jar directly from the Maven
repository. However, if you're using [Maven][] or [SBT][], you can just have
those tools do the job for you.

## Installing for Maven

If you're using [Maven][], just specify the artifact, and Maven will do the
rest for you:

* Group ID: `clapper.org`
* Artifact ID: `argot_2.9.1`
* Version: `0.3.7`
* Type: `jar`

Here's a sample Maven POM "dependency" snippet:

    <dependency>
      <groupId>org.clapper</groupId>
      <artifactId>argot_2.9.1</artifactId>
      <version>0.3.7</version>
    </dependency>

Version 0.3.7 is available for Scala 2.9.1, 2.9.0-1, 2.9.0, 2.8.2, 2.8.1 and
2.8.0.

For more information on using Maven and Scala, see Josh Suereth's
[Scala Maven Guide][].

## Using with SBT

#### 0.7.x

If you're using [SBT][] 0.7.x to compile your code, you can place the
following line in your project file (i.e., the Scala file in your
`project/build/` directory):

    val argot = "org.clapper" %% "argot" % "0.3.7"

#### 0.11.x

If you're using [SBT][] 0.11.x to compile your code, you can use the
following line in your `build.sbt` file (for Quick Configuration). If
you're using an SBT 0.11.x Full Configuration, you're obviously smart
enough to figure out what to do, on your own.

    libraryDependencies += "org.clapper" %% "argot" % "0.3.7"

Argot is also registered with [Doug Tangren][]'s excellent [ls.implicit.ly][]
catalog. If you use the `ls` SBT plugin, you can install Argot with

    sbt> ls-install argot

# Building from Source

## Source Code Repository

The source code for the Argot library is maintained on [GitHub][]. To
clone the repository, run this command:

    git clone git://github.com/bmc/argot.git

## Build Requirements

Building the Argot library requires [SBT][] 0.11.1 (or better). Install
SBT, as described at the SBT web site.

## Building Argot

Assuming you have an `sbt` shell script (or .BAT file, for *\[shudder\]*
Windows), run:

    sbt compile test package

The resulting jar file will be in the top-level `target` directory.

# Runtime Requirements

Argot requires the following libraries to be available at runtime, for
some, or all, of its methods.
 
* The [Grizzled Scala][] library

Maven and [SBT][] should automatically download these libraries for you.

# Using Argot

`ArgotParser` is a command-line parser and the main entry point for the
API. An `ArgotParser` embodies a representation of the command line: its
expected options and their value types, the expected positional parameters
and their value types, the name of the program, and other values.

Using Argot boils down to creating a command line specification and using it
to parse a command line. To accomplish that goal you:

* create an `ArgotParser` instance
* call its `option()`, `multiOption()` and `flag()` methods, to create
  specifications for the options
* call its `parameter()` and `multiParameter()` methods to create
  specifications for the positional parameters
* call its `parse()` method to parse the command line arguments.

Each of these steps is described further, below.

## Supported Syntax

`ArgotParser` supports:

* GNU-style long options ("--option value"), though it does *not* support
  the use of "=". That is, "--option=value" is *not* supported.
* POSIX-style short options ("-o value")
* The ability to combine short arguments ("-ovalue", "-xcf foo", etc.)
* The quasi-standard "--" argument to denote the end of the options list.
  (This argument isn't required, but it's useful if subsequent positional
  parameters start with a "-".)

## The *cooltool* Example

The remainder of this usage section discusses how to build a command-line
specification and parse it. We'll be creating the specification for a
fictitious tool called "cooltool", with the following usage (as generated
by Argot):

    cooltool: Version 1.0

    Usage: cooltool [OPTIONS] outputfile [input] ...

    OPTIONS

    -e emailaddr
    -e emailaddr
    --email emailaddr  Address to receive emailed results (May be specified
                       multiple times.)

    -i n
    --iterations n     Total iterations

    -n
    --noerror          Do not abort on error.

    -u username
    --user username    User to receive email. Email address is queried from
                       database. (May be specified multiple times.)

    -q
    --quiet
    -v
    --verbose          Increment (-v, --verbose) or decrement (-q, --quiet) the
                       verbosity level.

    PARAMETERS

    outputfile  Output file to which to write.

    input       Input files to read. If not specified, use stdin. (May be specified
                multiple times.)

## Creating the Parser

The constructor for the `ArgotParser` class takes five parameters, four of
which are optional:

* `programName`: A required string, specifying the name of the program (since
  the program name isn't available in the argument array).
* `compactUsage`: A optional boolean indicating whether the generated usage
  string should be compacted (i.e., stripped of extra newlines added for
  readability) or not. Default: `false`
* `outputWidth`: An integer that specifies the output width, in characters,
  used for word-wrapping the generated usage string. Default: 79
* `preUsage`: An `Option[String]`, specifying a message to precede the
  usage block in the generated usage string. Often, this prefix contains
  the utility name, version, and copyright. The string is wrapped on word
  boundaries, just like the usage message. Default: `None`
* `postUsage`: An `Option[String]`, specifying a message to follow the
  usage block in the generated usage string. The string is wrapped on word
  boundaries, just like the usage message. Default: `None`
  
For *cooltool*, our `ArgotParser` looks like this:

    import org.clapper.argot._

    val parser = new ArgotParser("cooltool", preUsage=Some("Version 1.0"))

That line of code defines a parser with a prefix message, but no post-usage
message, using the default output width and a non-compact usage string.

## Specifying the Options

Options may be specified in any order.

Options have one or more names. Single-character names are assumed to be
preceded by a single hyphen (`-`); multicharacter names are assumed to be
preceded by a double hyphen (`--`). Single character names can be combined,
POSIX-style.

Given the [*cooltool* usage][], the following command lines are identical:

    cooltool -v -n -i 10 out
    cooltool --verbose --noerror --iterations 10 --output out
    cooltool -nvi10 out
    cooltool -nv -i10 out

[*cooltool* usage]: #usage

There are three kinds of options:

* *Single-value options* are options that take only one value.
* *Multi-value options* are options that can take multiple values.
* *Flag options* are options that take no values (i.e., the presence or
  absence of the option itself *is* the value).

Each type of is discussed further, below.

### Single-value Options

A single-value option is one that takes a single value. The first
occurrence of the option on the command line sets the value. Any subsequent
occurrences replace the previously set values.

Single-value options are defined with the `option()` methods, which return
a typed instance of the `SingleValueOption` class. The `SingleValueOption`
object contains a `value` field, of type `Option[T]`. If the option doesn't
appear on the command line, `value` will be `None`. Otherwise, it'll be set
to `Some(value)`. There's no provision for assigning a default value, since
that can be accomplished via the `Option` class's `getOrElse()` method.

For *cooltool*, there is one single-value option:

    -i iterations
    --iterations iterations

To create this option, use the following code fragment:

{% highlight scala %}
import ArgotConverters._

val iterations = parser.option[Int](List("i", "iterations"), "n",
                                    "total iterations")
{% endhighlight %}

There are several things to note here:

1. The `option` method takes a type parameter. In this case, we've supplied
   an `Int`, indicating that we want to convert the value to an `Int`.
2. The valid names for the option are specified in the initial parameter, a
   list. The single-character name, "i", corresponds to a `-i` option on the
   command line. The multicharacter name, "iterations", corresponds to
   `--iterations`.

### Introducing Automatic Conversions

The actual definition of the `option` method is:

{% highlight scala %}
def option[T](names: List[String], valueName: String, description: String)
             (implicit convert: (String, SingleValueOption[T]) => T):
    SingleValueOption[T] =
{% endhighlight %}

Note the second parameter list, with the implicit `convert` parameter. This
parameter specifies a conversion function that will convert the string
value into the desired type (`Int` in our case). Argot has some pre-defined
conversion functions for common types, in the
`org.clapper.argot.ArgotConverters` module. If you pull the contents of
that module into your namespace, then you don't have to specify a conversion
function for common types. This import:

{% highlight scala %}
import ArgotConverters._
{% endhighlight %}
    
makes those built-in implicit conversion functions available.

You *can* supply your own function, however. We could just as easily have
defined `iterations` like this:

{% highlight scala %}
val iterations = parser.option[Int](List("i", "iterations"), "n",
                                    "total iterations") {
  (sValue, opt) =>
    
  try {
    sValue.toInt
  }

  catch {
    case _: NumberFormatException =>
      throw new ArgotConversionException(
        "Option " + opt.name + ": \"" + sValue + "\" isn't a number."
      )
  }
}
{% endhighlight %}

We could also have used an implicit function:

{% highlight scala %}
implicit def convertIterations(sValue: String, opt: CommandLineArgument[]): Int = {
  try {
    sValue.toInt
  }
  catch {
    case _: NumberFormatException =>
      throw new ArgotConversionException(
        "Option " + opt.name + ": \"" + sValue + "\" isn't a number"
      )
  }
}
val iterations = parser.option[Int](List("i", "iterations"), "n",
                                    "total iterations")
{% endhighlight %}

Defining the conversion function as an `implicit` is useful if you want to
use the same conversion function with multiple parameters.

The built-in Argot  `String`-to-`Int` conversion function looks pretty much
like the `convertIterations()` function, above.

All the option-specification and parameter-specification methods support
implicit conversion functions.

Some common reasons to supply your own conversion function include:

* You want to convert the option to a type that isn't supported by the
  standard Argot conversion functions.
* You want to do some validation on the value.

We'll see some examples of both of these cases in subsequent sections.

### Multi-value Options

A multi-value option is one that takes a single value, but if it appears
multiple times on the command line, each occurrence adds its value to the
list of already accumulated values for the option. The values are stored in
a Scala sequence. Each occurrence of the option on the command line adds
the associated value to the sequence. If the option never appears on the
command line, its value will be an empty list.

Multi-value options are defined with the `multiOption()` methods, which a
typed instance of the `MultiValueOption` class. The `MultiValueOption`
object contains a `value` field, of type `Seq[T]`. If the option doesn't
appear on the command line, `value` will be `Nil`. Otherwise, it will
contain all the values that were present for each invocation of the option
on the command line. There's no provision for assigning a default value.

For *cooltool*, there are two multi-value options: `--email` and `--user`.
One takes a string and can use the default conversion function. The other
takes an email address and supplies its own conversion function, to check
the validity of the supplied parameter. The code for each is shown below:

{% highlight scala %}
val users = parser.multiOption[String](
    List("u", "user"), "username",
    "User to receive email. Email"address is queried from database."
)

val emails = parser.multiOption[String](
    List("e", "email"), "emailaddr", "Address to receive emailed results."
) {
  (s, opt) =>

   val ValidAddress = """^[^@]+@[^@]+\.[a-zA-Z]+$""".r
   ValidAddress.findFirstIn(s) match {
    case None    => parser.usage("Bad email address \"" + s +
                                 "\" for " + opt.name + " option.")
    case Some(_) => s
  }
}
{% endhighlight %}

### Flag Options

Flag options take no values; they either appear on the command line, or
they don't. Typically, flag options are associated with boolean value,
though Argot will permit you to associate them with any type you choose.

Flag options permit you to segregate the option names into *on* names and
*off* names. With boolean flag options, the *on* names set the value to
`true`, and the *off* names set the value to values. With typed flag
options, what happens depends on the conversion function.

*cooltool* has two flag options. The `--noerror` option is a simple boolean
flag; specify `--noerror` (or `-n`), and the flag is set to `true`. Otherwise,
the flag is unset. The verbosity flag option, however, is an integer. Specify
`-v` or `--verbose`, and the verbosity level gets incremented; specify `-q`
or `--quiet`, and the verbosity level gets decremented.

The code for both options follows:

{% highlight scala %}
val noError = parser.flag[Boolean](List("n", "noerror"),
                                   "Do not abort on error.")

val verbose = parser.flag[Int](List("v", "verbose"),
                               List("q", "quiet"),
                               "Increment (-v, --verbose) or " +
                               "decrement (-q, --quiet) the " +
                               "verbosity level.") {
  (onOff, opt) =>

  import scala.math

  val currentValue = opt.value.getOrElse(0)
  val newValue = if (onOff) currentValue + 1 else currentValue - 1
  math.max(0, newValue)
}
{% endhighlight %}

## Positional Parameters

Positional parameters are the parameters following options. They have the
following characteristics.

* Like options, they can be typed.
* Unlike options, they must be defined in the order they are expected
  to appear on the command line.
* The final positional parameter, and only the final parameter,
  can be permitted (by the calling program) to have multiple values.
* Positional parameters can be optional, as long as all required
  positional parameters come first.

*cooltool* has two position parameters.

* `output` is a required string and comes first. Since it's a string, the
  code can use the default conversion function.
* `input` is optional and may be specified multiple times; it represents
  input files to be read. We want to verify that each input file exists,
  so the parameter type is `File` and a custom conversion function verifies
  that each file exists.

Here's the code for each parameter:

{% highlight scala %}
val output = parser.parameter[String]("outputfile",
                                      "Output file to which to write.",
                                      false)

val input = parser.multiParameter[File]("input",
                                        "Input files to read. If not " +
                                        "specified, use stdin.",
                                        true) {
  (s, opt) =>

  val file = new File(s)
  if (! file.exists)
      parser.usage("Input file \"" + s + "\" does not exist.")

  file
}
{% endhighlight %}

## Putting It All Together

The entire main program for *cooltool* looks like this:

{% highlight scala %}
package org.clapper.argot
import java.io.File
import scala.math

object CoolTool {
  // Argument specifications

  import ArgotConverters._

  val parser = new ArgotParser(
    "test",
    preUsage=Some("ArgotTest: Version 0.1. Copyright (c) " +
                  "2012, Brian M. Clapper. Pithy quotes go here.")
  )

  val iterations = parser.option[Int](List("i", "iterations"), "n",
                                      "Total iterations")
  val verbose = parser.flag[Int](List("v", "verbose"),
                                 List("q", "quiet"),
                                 "Increment (-v, --verbose) or " +
                                 "decrement (-q, --quiet) the " +
                                 "verbosity level.") {
    (onOff, opt) =>

    import scala.math

    val currentValue = opt.value.getOrElse(0)
    val newValue = if (onOff) currentValue + 1 else currentValue - 1
    math.max(0, newValue)
  }

  val noError = parser.flag[Boolean](List("n", "noerror"),
                                     "Do not abort on error.")
  val users = parser.multiOption[String](List("u", "user"), "username",
                                         "User to receive email. Email " +
                                         "address is queried from " +
                                         "database.")

  val email = parser.multiOption[String](List("e", "email"), "emailaddr",
                                         "Address to receive emailed " +
                                         "results.") {
    (s, opt) =>

    val ValidAddress = """^[^@]+@[^@]+\.[a-zA-Z]+$""".r
    ValidAddress.findFirstIn(s) match {
      case None    => parser.usage("Bad email address \"" + s +
                                   "\" for " + opt.name + " option.")
      case Some(_) => s
    }
  }

  val output = parser.parameter[String]("outputfile",
                                        "Output file to which to write.",
                                        false)

  val input = parser.multiParameter[File]("input",
                                          "Input files to read. If not " +
                                          "specified, use stdin.",
                                          true) {
    (s, opt) =>

    val file = new File(s)
    if (! file.exists)
      parser.usage("Input file \"" + s + "\" does not exist.")

    file
  }

  // The guts of the program (omitted here)
  def runCoolTool = {
    ...
  }

  // Main program
  def main(args: Array[String]) {
    try {
      parser.parse(args)
      runCoolTool
    }

    catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}
{% endhighlight %}

## Resetting the Parser

If you want to re-use the same `ArgotParser`, you must reset its parameter
and option values, clearing them; otherwise, they will retain the parsed
values from the previous parse. The `ArgotParser` class provides a
convenient way to reset everything:

{% highlight scala %}
val p = new ArgotParser(...)

...

p.parse(args)

...

p.reset()  // resets all internal state
{% endhighlight %}

## API Documentation

The Scaladoc-generated the [API documentation][] is available locally.
In addition, you can generate your own version with:

    sbt doc

The change log is [here][changelog].

# Similar Work

There are other Scala- and Java-based argument parsers available. While this
list is by no means exhaustive, here are some examples:

* James Strachan's [scopt][] (Scala)
* [optional][] (Scala)
* [jopt-simple][] (Java)
* [parse-cmd][] (Java and Scala; hasn't been updated since July, 2009)
* Cédric Beust's annotation-driven [JCommander][] (Java)
* [JewelCli][], another annotation-driven API (Java)

[optional]: https://github.com/alexy/optional
[scopt]: http://github.com/jstrachan/scopt
[jopt-simple]: http://jopt-simple.sourceforge.net/
[parse-cmd]: http://code.google.com/p/parse-cmd/
[JewelCli]: http://jewelcli.sourceforge.net/usage.html
[JCommander]: http://beust.com/jcommander/

This [Stack Overflow article][] on the topic is also worth reading.

[Stack Overflow article]: http://stackoverflow.com/questions/2315912/scala-best-way-to-parse-command-line-parameters-cli

# Author

[Brian M. Clapper][]

# Contributing to Argot

Argot is still under development. If you have suggestions or
contributions, feel free to fork the [Argot repository][], make your
changes, and send me a pull request.

# Copyright and License

Argot is copyright &copy; 2010-2011 Brian M. Clapper and is released under a
[BSD License][].

# Patches

I gladly accept patches from their original authors. Feel free to email
patches to me or to fork the [Argot repository][] and send me a pull
request. Along with any patch you send:

* Please state that the patch is your original work.
* Please indicate that you license the work to the Argot project
  under a [BSD License][].

[Scala]: http://www.scala-lang.org/
[GitHub repository]: http://github.com/bmc/argot
[Argot repository]: http://github.com/bmc/argot
[GitHub]: http://github.com/bmc/
[API documentation]: api/
[BSD License]: license.html
[Brian M. Clapper]: mailto:bmc@clapper.org
[SBT]: http://code.google.com/p/simple-build-tool
[SBT cross-building]: http://code.google.com/p/simple-build-tool/wiki/CrossBuild
[Scala Tools Maven repository]: http://www.scala-tools.org/repo-releases/
[Scala Maven Guide]: http://www.scala-lang.org/node/345
[Maven]: http://maven.apache.org/
[changelog]: CHANGELOG.html
[Grizzled Scala]: http://software.clapper.org/grizzled-scala/
[bmc@clapper.org]: mailto:bmc@clapper.org
[Maven central repository]: http://search.maven.org/
[ls.implicit.ly]: http://ls.implicit.ly
