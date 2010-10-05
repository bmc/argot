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
* extensibility

# WARNING, WARNING, DANGER, DANGER!

**Argot is still under development.** It's up here on GitHub because... well,
dammit, I needed a place to check it in, and it might as well be here.

When Argot is released, a release tag will magically appear, this page will
self-update, and a source download will show up in the [GitHub repository][]
*Downloads* tab. Until then, there's no guarantee that Argot will work
properly, since it's still a work in progress.

In fact, until then, you should consider Argot to be alpha code. It's working
for me, but you *must* consider the possibility that it might:

* do a [fandango][] all over your program
* generate random [bogons][] (of various [flavors][], [charmed][] or otherwise)
* [open your sluices at both ends][]
* [eat your firstborn child][]
* [cough and die][];
* or otherwise do the [Wrong Thing][].

None of these things is especially likely, mind you, but you *have* been
warned.

[fandango]: http://catb.org/jargon/html/F/fandango-on-core.html
[bogons]: http://catb.org/jargon/html/B/bogon.html
[charmed]: http://en.wikipedia.org/wiki/Charm_quark
[flavors]: http://en.wikipedia.org/wiki/Flavour_(particle_physics)
[open your sluices at both ends]: http://www.phespirit.info/montypython/australian_table_wines.htm
[eat your firstborn child]: http://www.facebook.com/pages/I-will-eat-your-firstborn-child/285767234279
[cough and die]: http://catb.org/jargon/html/C/cough-and-die.html
[Wrong Thing]: http://catb.org/jargon/html/W/Wrong-Thing.html


# Installation

The easiest way to install the Argot library is to download a pre-compiled
jar from the [Scala Tools Maven repository][]. However, you can also get
certain build tools to download it for you automatically.

## Installing for Maven

If you're using [Maven][], you can simply tell Maven to get Argot from the
[Scala Tools Maven repository][]. The relevant pieces of information are:

* Group ID: `clapper.org`
* Artifact ID: `argot_2.8.0`
* Version: `0.1`
* Type: `jar`
* Repository: `http://scala-tools.org/repo-releases`

Here's a sample Maven POM "dependency" snippet:

    <repositories>
      <repository>
        <id>scala-tools.org</id>
          <name>Scala-tools Maven2 Repository</name>
          <url>http://scala-tools.org/repo-releases</url>
      </repository>
    </repositories>

    <dependency>
      <groupId>org.clapper</groupId>
      <artifactId>argot_2.8.0</artifactId>
      <version>0.1</version>
    </dependency>

For more information on using Maven and Scala, see Josh Suereth's
[Scala Maven Guide][].

## Using with SBT

If you're using [SBT][] to build your code, place the following line in
your project file (i.e., the Scala file in your `project/build/`
directory):

    val argot = "org.clapper" %% "argot" % "0.1"

**NOTES:**

1. The first doubled percent is *not* a typo. It tells SBT to treat
   Argot as a cross-built library and automatically inserts the Scala
   version you're using into the artifact ID. It will *only* work if you
   are building with Scala 2.8.0. See the [SBT cross-building][] page for
   details.

# Building from Source

## Source Code Repository

The source code for the Argot library is maintained on [GitHub][]. To
clone the repository, run this command:

    git clone git://github.com/bmc/argot.git

## Build Requirements

Building the Argot library requires [SBT][]. Install SBT, as described
at the SBT web site.

## Building Argot

Assuming you have an `sbt` shell script (or .BAT file, for *\[shudder\]*
Windows), first run:

    sbt update

That command will pull down the external jars on which the Argot
library depends. After that step, build the library with:

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

## Supported Syntax

`ArgotParser` supports GNU-style option parsing, with both long ("--")
options and short ("-") options. Short options may be combined, POSIX-style.
The end of the options list may be signaled via a special "--" argument;
this argument isn't required, but it's useful if subsequent positional
parameters start with a "-".

## Specifying the Options

Options may be specified in any order.

Options have one or more names. Single-character names are assumed to
be preceded by a single hyphen ("-"); multicharacter names are assumed to
be preceded by a double hyphen ("--"). Single character names can be
combined, POSIX-style. For instance, assume that a program called "foo"
takes the following options:

* "-f" or "--output" specifies an output file
* "-v" or "--verbose" specifies that verbose output is required
* "-n" or "--noerror" specifies that errors are to be ignored

Given those options, the following command lines are identical:

    foo -v -n -f out
    foo --verbose --noerror --output out
    foo -nvfout
    foo -nvf out

There are three kinds of options:

* Single-value options
* Multi-value options
* Flag Options

Each type of is discussed further, below.

### Single-value Options

A single-value option is one that takes a single value. The first
occurrence of the option on the command line sets the value. Any
subsequent occurrences replace the previously set values. The option's
value is stored in a `Option[T]`. If the option isn't specified on the
command line, its value will be `None`. Otherwise, it'll be set to
`Some(value)`. There's no provision for assigning a default value,
since that can be accomplished via the `Option` class's `getOrElse()`
method.

Single-value options are defined with the `option()` methods.

### Multi-value Options

A multi-value option is one that takes a single value, but can appear
multiple times on the command line, with each occurrence adding its
value to the list of already accumulated values for the option. The
values are stored in a Scala sequence. Each occurrence of the option on
the command line adds the associated value to the sequence. If the
option never appears on the command line, its value will be an empty list.

Multi-value options are defined with the `multiOption()` methods.

### Flag Options

Flag options take no values; they either appear or not. Typically, flag
options are associated with boolean value, though Argot will permit you
to associate them with any type you choose.

Flag options permit you to segregate the option names into *on* names and
*off* names. With boolean flag options, the *on* names set the value to
`true`, and the *off* names set the value to values. With typed flag
options, what happens depends on the conversion function.

### Positional Parameters

Positional parameters are the parameters following options. They have the
following characteristics.

* Like options, they can be typed.
* Unlike options, they must be defined in the order they are expected
  to appear on the command line.
* The final positional parameter, and only the final parameter,
  can be permitted (by the calling program) to have multiple values.
* Positional parameters can be optional, as long as all required
  positional parameters come first.


*more coming*

## Author

Brian M. Clapper, [bmc@clapper.org][]

## Copyright and License

The Grizzled Scala Library is copyright &copy; 2009-2010 Brian M. Clapper
and is released under a [BSD License][].

## Patches

I gladly accept patches from their original authors. Feel free to email
patches to me or to fork the [GitHub repository][] and send me a pull
request. Along with any patch you send:

* Please state that the patch is your original work.
* Please indicate that you license the work to the PROJECT project
  under a [BSD License][].

[downloads area]: http://github.com/bmc/PROJECT/downloads

## API Documentation

The Scaladoc-generated the [API documentation][] is available locally.
In addition, you can generate your own version with:

    sbt doc

# Author

[Brian M. Clapper][]

# Contributing to Argot

Argot is still under development. If you have suggestions or
contributions, feel free to fork the [Argot repository][], make your
changes, and send me a pull request.

# Copyright and License

Argot is copyright &copy; 2010 Brian M. Clapper and is released under a
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
[Grizzled Scala]: http://bmc.github.com/grizzled-scala/
[bmc@clapper.org]: mailto:bmc@clapper.org
