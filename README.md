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

---
**WARNING, WARNING, DANGER, DANGER!**

**Argot is still under development.** It's checked in here because... well,
dammit, I needed a place to check it in, and it might as well be here.

When Argot is released, a release tag will magically appear, and a source
download will magically show up in the [GitHub repo][] *Downloads* tab. Until
then, there's no guarantee that Argot will work properly, since it's still
a work in progress.

In fact, until then, you should consider Argot to be in a #FAIL state. It
may:

* do a [fandango][] all over your program
* generate random [bogons][] (of various [flavors][], [charmed][] or otherwise)
* [open your sluices at both ends][]
* [eat your firstborn child][]
* [cough and die][];
* or otherwise do the [Wrong Thing][].

None of these things is especially likely, mind you, but you *have* been
warned.

---

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
