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
class ArgotMultiOptionTest extends FunSuite {
  import ArgotConverters._

  test("multi-value option success") {
    val parser = new ArgotParser("test")
    val opt = parser.multiOption[String](
      List("s", "something"), "something", "Some value"
    )

    val data = List(
      (Seq("something"),  Array("-s", "something")),
      (Seq("foo"),        Array("--something", "foo")),
      (Nil,               Array.empty[String]),
      (Seq("foo", "bar"), Array("-s", "foo", "-s", "bar"))
    )

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expect(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        opt.value
      }
    }
  }

  test("multi-value option failure") {
    val parser = new ArgotParser("test")
    val opt = parser.multiOption[String](
      List("s", "something"), "something", "Some value"
    )

    val data = List(Array("-f"),
                    Array("-s"))

    for (args <- data) {
      intercept[ArgotUsageException] {
        parser.parse(args)
      }
    }
  }

  test("integer multi-option") {
    val parser = new ArgotParser("test")
    val opt = parser.multiOption[Int]("i", "someint", "integer")

    val data = List(
      (Seq(3),        Array("-i", "3")),
      (Nil,           Array.empty[String]),
      (Seq(3, 0),     Array("-i", "3", "-i", "0")),
      (Seq(1, 10, 1), Array("-i", "1", "-i", "10", "-i", "1"))
    )

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expect(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        opt.value
      }
    }
  }

  test("custom type multi-option") {
    class Foo(val i: Int)

    val parser = new ArgotParser("test")
    val opt = parser.multiOption[Foo]("i", "n", "some number") {
      (s, opt) =>

        new Foo(s.toInt)
    }

    val data = List(
      (List(3),         Array("-i", "3")),
      (Nil,             Array.empty[String]),
      (List(3, 0),      Array("-i", "3", "-i", "0")),
      (List(1, 10, 1),  Array("-i", "1", "-i", "10", "-i", "1"))
    )

    for ((expected, args) <- data) {
      parser.reset()
      parser.parse(args)
      expect(expected, args.mkString("[", ", ", "]") + " -> " + expected) {
        opt.value.map(_.i)
      }
    }
  }
}
