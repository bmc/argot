/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010-2012, Brian M. Clapper
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

   * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

   * Neither the names "clapper.org", "Argot", nor the names of its
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

package org.clapper.argot

/**
 * Conversion functions that can be used to satisfy the implicit conversions
 * specified to the various specification functions in the `ArgotParser` class.
 * If you import this namespace, you'll get a bunch of implicit conversion
 * functions that the Scala compiler will automatically use, for the
 * various definition functions in `ArgotParser`.
 *
 * The conversion functions all take the `CommandLineArgument` for which the
 * value applies. This serves two purposes. First, it provides more information
 * for error messages. Second, it makes the conversion functions less ambiguous.
 */
object ArgotConverters {
  /** Convert a string value into an integer. A non-numeric string value
   * will cause an error.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the integer
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertInt(s: String, opt: CommandLineArgument[Int]): Int = {
    Conversions.parseInt(s, opt.name)
  }

  /** Convert a string value into a long. A non-numeric string value
   * will cause an error.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the long integer
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertLong(s: String, opt: CommandLineArgument[Long]): Long = {
    Conversions.parseLong(s, opt.name)
  }

  /** Convert a string value into a short. A non-numeric string value
   * will cause an error.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the short
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertShort(s: String, opt: CommandLineArgument[Short]):
  Short = {
    Conversions.parseShort(s, opt.name)
  }

  /** Convert a string value into a float. A non-numeric string value
   * will cause an error.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the float.
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertFloat(s: String, opt: CommandLineArgument[Float]):
  Float = {
    Conversions.parseFloat(s, opt.name)
  }

  /** Convert a string value into an double. A non-numeric string value
   * will cause an error.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the double.
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertDouble(s: String, opt: CommandLineArgument[Double]):
  Double = {
    Conversions.parseDouble(s, opt.name)
  }

  /** Convert a string value into a character. A string that is empty or
   * is longer than one character in length will cause an error.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the character
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertChar(s: String, opt: CommandLineArgument[Char]): Char = {
    Conversions.parseChar(s, opt.name)
  }

  /** Convert a string value into a byte value. A non-numeric string value
   * will cause an error, as will a value that is outside the range
   * [0, 255].
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the integer
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertByte(s: String, opt: CommandLineArgument[Byte]): Byte = {
    Conversions.parseByte(s, opt.name)
  }

  /** Convert a string value into a string. This function is a no-op.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the integer
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertString(s: String, opt: CommandLineArgument[String]):
  String = {
    s
  }

  /** Convert a value for a flag option. This function is primarily a
   * no-op that exists to satisfy the implicit parameter for the
   * `ArgotParser.flag()` methods.
   *
   * @param onOff  the value to be returned
   * @param opt    the command line argument to which the value applies
   *
   * @return the value of `onOff`
   */
  implicit def convertFlag[Boolean](onOff: Boolean,
                                    opt: FlagOption[Boolean]): Boolean = {
    onOff
  }

  /** Convert a string value into a sequence, adding the result to the
   * supplied `MultiValueOption` object's `value` field. The string is
   * split into multiple strings via the supplied `parse()` function
   * parameter; the parameter is marked implicit, so that it can be
   * satisfied automatically.
   *
   * If the `ArgotConverters` name space is in scope, then the default
   * implicit function that satisfies the parameter simply returns the
   * string, unparsed, thus resulting in the argument string being
   * concatenated, as is, to the `MultiValueOption`. This behavior is
   * generally the one most often used; however, it's possible to substitute
   * other parsing functions that (for instance) split the string based on
   * a delimiter.
   *
   * @param s   the string value to convert
   * @param opt the command line argument to which the value applies
   *
   * @return the integer
   *
   * @throws ArgotConversionException conversion error
   */
  implicit def convertSeq[T](s: String, opt: MultiValueOption[T])
                            (implicit parse: (String, String) => T): Seq[T] = {
    opt.value :+ parse(s, opt.name).asInstanceOf[T]
  }
}
