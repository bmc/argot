---
title: Argot--A command-line parser for Scala
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
self-update, and a source download will show up in the [GitHub repo][]
*Downloads* tab. Until then, there's no guarantee that Argot will work
properly, since it's still a work in progress.

In fact, until then, you should consider Argot to be in a #FAIL state. It
may:

* do a [fandango][] all over your program
* generate random [bogons][] (of various [flavors][], [charmed][] or otherwise)
* [open your sluices at both ends][]
* [eat your firstborn child][]
* [cough and die][];
* or otherwise do the [Wrong Thing][].

None of these things is especially likely, mind you, but you *have* been
warned.)

# More to come

This document will be expanded to include all the good things you need to
know, including:

* how to install Argot
* how to build Argot from source
* how to use Argot in your programs
* how to determine the airspeed velocity of an unladen swallow

[Scala]: http://www.scala-lang.org/
[GitHub repo]: http://github.com/bmc/argot/
[fandango]: http://catb.org/jargon/html/F/fandango-on-core.html
[bogons]: http://catb.org/jargon/html/B/bogon.html
[charmed]: http://en.wikipedia.org/wiki/Charm_quark
[flavors]: http://en.wikipedia.org/wiki/Flavour_(particle_physics)
[open your sluices at both ends]: http://www.phespirit.info/montypython/australian_table_wines.htm
[eat your firstborn child]: http://www.facebook.com/pages/I-will-eat-your-firstborn-child/285767234279
[cough and die]: http://catb.org/jargon/html/C/cough-and-die.html
[Wrong Thing]: http://catb.org/jargon/html/W/Wrong-Thing.html



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

[BSD License]: license.html
[GitHub repository]: http://github.com/bmc/PROJECT
[GitHub]: http://github.com/bmc/
[downloads area]: http://github.com/bmc/PROJECT/downloads
[*clapper.org* Maven repository]: http://maven.clapper.org/org/clapper/
[Maven]: http://maven.apache.org/
[bmc@clapper.org]: mailto:bmc@clapper.org
