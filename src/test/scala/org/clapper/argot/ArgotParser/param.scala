/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

   * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

   * Neither the names "clapper.org", "Scalasti", nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ---------------------------------------------------------------------------
*/

import org.scalatest.FunSuite
import org.clapper.argot._

/** Tests the grizzled.io functions.
  */
class ArgotParameterTest extends FunSuite {
  import ArgotConverters._

  test("one required argument") {
    val parser = new ArgotParser("test")
    val opt = parser.option[String](List("s", "something"),
                                    "something", "Some value")
    val req = parser.parameter[String]("foo", "some param", optional=false)

    val data = List(
      (Some("something"), Array("-s", "something", "something")),
      (Some("param"),     Array("--something", "foo", "param")),
      (Some("foo"),       Array("-s", "foo", "-s", "bar", "foo")),
      (Some("foo"),       Array("foo")),
      (Some("--foo"),     Array("-s", "something", "--", "--foo"))
    )

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expectResult(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        req.value
      }
    }
  }

  test("required argument failure") {
    val parser = new ArgotParser("test")
    val req = parser.parameter[String]("foo", "some param", optional=false)

    val data = List(Array("-f"),
                    Array("-s"),
                    Array("-s", "something"),
                    Array.empty[String])

    for (args <- data) {
      intercept[ArgotUsageException] {
        parser.parse(args)
      }
    }

    val opt = parser.option[String](
      List("s", "something"), "something", "Some value"
    )

    for (args <- data) {
      intercept[ArgotUsageException] {
        parser.parse(args)
      }
    }
  }

  test("optional argument") {
    val parser = new ArgotParser("test")
    val opt = parser.option[String](
      List("s", "something"), "something", "Some value"
    )
    val req = parser.parameter[String]("foo", "some param", optional=true)

    val data = List(
      (Some("something"), Array("-s", "something", "something")),
      (Some("param"),     Array("--something", "foo", "param")),
      (Some("foo"),       Array("-s", "foo", "-s", "bar", "foo")),
      (Some("foo"),       Array("foo")),
      (Some("--foo"),     Array("-s", "something", "--", "--foo")),
      (None,              Array("-s", "something")),
      (None,              Array("-s", "something", "--"))
    )

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expectResult(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        req.value
      }
    }
  }

  test("required + optional argument") {
    val parser = new ArgotParser("test")
    val opt = parser.option[String](
      List("s", "something"), "something", "Some value"
    )
    val foo = parser.parameter[String]("foo", "some param", optional=false)
    val bar = parser.parameter[String]("bar", "some param", optional=true)

    val data = List(
      (Some("abc"),   None,        Array("-s", "s", "abc")),
      (Some("foo"),   Some("bar"), Array("-s", "foo", "foo", "bar")),
      (Some("foo"),   None,        Array("foo"))
    )

    for ((expected_foo, expected_bar, args) <- data) {
      parser.reset()
      parser.parse(args)
      val prefix =
        expectResult((expected_foo, expected_bar),

               args.mkString("[", ", ", "]") + " -> " +
               "(" + expected_foo + ", " + expected_bar + ")") {
                 (foo.value, bar.value)
               }
    }
  }

  test("specification error 1") {
    val parser = new ArgotParser("test")

    intercept[ArgotSpecificationError] {
      // Optional parameter, followed by required parameter.

      parser.parameter[String]("foo", "some param", optional=true)
      parser.parameter[String]("bar", "some param", optional=false)
    }
  }

  test("specification error 2") {
    val parser = new ArgotParser("test")

    intercept[ArgotSpecificationError] {
      // Multi-parameter, not last.

      parser.multiParameter[String]("foo", "some param", optional=true)
      parser.parameter[String]("bar", "some param", optional=true)
    }
  }

  test("multi-valued parameter") {
    val parser = new ArgotParser("test")
    parser.option[String]("s", "string", "some string")
    val param = parser.multiParameter[Int]("count", "some count",
                                           optional=true)

    val data = List((Seq(1),       Array("1")),
                    (Seq(1, 2),    Array("1", "2")),
                    (Seq(3),       Array("-s", "s", "3")),
                    (Nil,          Array("-s", "foo")),
                    (Nil,          Array.empty[String]))

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expectResult(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        param.value
      }
    }
  }

  test("custom type parameter") {
    class MyParam(val i: Int)

    val parser = new ArgotParser("test")
    parser.option[String]("s", "string", "some string")

    val param = parser.multiParameter[MyParam]("count", "some count",
                                               optional=true) {
      (s, opt) =>

        new MyParam(s.toInt)
    }

    val data = List((Seq(1),       Array("1")),
                    (Seq(1, 2),    Array("1", "2")),
                    (Seq(3),       Array("-s", "s", "3")),
                    (Nil,          Array("-s", "foo")),
                    (Nil,          Array.empty[String]))

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expectResult(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        param.value.map(_.i)
      }
    }
  }
}
