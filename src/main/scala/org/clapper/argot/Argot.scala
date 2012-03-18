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

/** Argot is a command-line argument parsing API for Scala.
  */
package org.clapper.argot

import scala.reflect.Manifest
import scala.util.matching.Regex
import scala.annotation.tailrec

/**
  * Container for shared types.
  */
object Types {
  type Converter[T] = (String) => Either[String, T]
  type FlagConverter[T] = (Boolean, String) => Either[String, T]
}

import Types._

/** Base trait for all option and parameter classes, `Argument`
  * contains common methods and values.
  *
  * @tparam T  the type associated with the argument
  */
trait Argument[T] {

  /** The argument's description, displayed in the usage message.
    */
  val description: String

  /** Whether or not the argument has an associated value. For instance,
    * parameters have values, and non-flag options have values. Flag options,
    * however, do not.
    */
  val hasValue: Boolean

  /** Displayable name for the argument, used in the usage message.
   *
   * @return the name
   */
  def name: String

  /** The standard `equals()` method.
   *
   * @param o  some other object
   *
   * @return `true` if the other object is the same class and is equivalent,
   *         `false` if not.
   */
  override def equals(o: Any): Boolean = {
    o match {
      case that: Argument[_] =>
        (this.getClass == that.getClass) && (this.key == that.key)
      case _ =>
        false
    }
  }

  /** Calculate the hash code for the object. The default implementation
   * returns the hash code of the key.
   *
   * @return the hash code
   *
   * @see #key
   */
  override def hashCode = key.hashCode

  /** Return an object that represents the key for this parameter, suitable
   * for hashing, sorting, etc.
   *
   * @return the key
   */
  protected def key: Any
}

/**
 * The `HasValue` trait is mixed into option and parameter classes that
 * support one or more associated values of type `T`.
 *
 * @tparam T  the value type
 *
 * @see SingleValueArg
 * @see MultiValueArg
 */
trait HasValue[T] extends Argument[T] {
  /** Always `true`, indicating that `HasValue` classes always have an
   * associated value.
   */
  val hasValue: Boolean = true

  /** All options and values with parameters must have a placeholder name
   * for the value, used in generating the usage message.
   */
  val valueName: String

  /** Whether or not the class supports multiple values (e.g., a sequence)
   * or just one.
   */
  val supportsMultipleValues: Boolean
}

/**
 * `SingleValueArg` is a refinement of the `HasValue` trait, specifically
 * for arguments (options or parameters) that take only a single value.
 * This trait exists primarily as a place for shared logic and values
 * for the option- and parameter-specific subclasses.
 *
 * @tparam T  the value type
 *
 * @see SingleValueOption
 * @see SingleValueParameter
 */
trait SingleValueArg[T] extends HasValue[T] {
  private var optValue: Option[T] = None

  val supportsMultipleValues = false
}

/**
 * `MultiValueArg` is a refinement of the `HasValue` trait, specifically
 * for arguments (options or parameters) that take multiple values of type
 * `T`. Each instance of the parameter on the command line adds to the
 * sequence of values in associated `MultiValueArg` object.
 *
 * This trait exists primarily as a place for shared logic and values
 * for the option- and parameter-specific subclasses.
 *
 * @tparam T  the value type
 *
 * @see MultiValueOption
 * @see MultiValueParameter
 */
trait MultiValueArg[T] extends HasValue[T] {
  val supportsMultipleValues = true
}

/**
 * `ArgotOption` is the base trait for all option classes.
 *
 * @tparam T  the value type
 *
 * @see SingleValueOption
 * @see MultiValueOption
 */
trait ArgotOption[T] extends Argument[T] {
  /** List of option names, both long (multi-character) and short
   * (single-character).
   */
  val names: Seq[String]

  /** Return a suitable name for the option. The returned name will
   * have a "-" or "--" prefix, depending on whether it's long or short.
   * It will be based on the first option in the list of option names.
   *
   * @return the option name
   */
  def name = names(0) match {
    case s: String if s.length > 1  => "--" + s
    case s: String if s.length == 1 => "-" + s
  }

  /** Retrieve a parsed value of the option.
    *
    * @param parsed  The parsed argument object
    *
    * @return `None`, if the parameter doesn't exist at all.
    *         `Some(Left(error))` if there was an error parsing the parameter.
    *         `Some(Right(value))` if a legal value was parsed
    */
  def get(p: ParsedParameters): Option[Either[String, T]] = {
    p.options.get(this).map(e => e match {
      case Right(value)  => Right[String, T](value.asInstanceOf[T])
      case Left(message) => Left[String, T](message)
    })
  }

  /** Get a printable name for this object.
   *
   * @return the printable name
   */
  override def toString = "option " + name

  /** Return an object that represents the key for this parameter, suitable
   * for hashing, sorting, etc. They key for a command line option is the
   * result of calling `name()`.
   *
   * @return the key
   */
  protected def key = name
}

/**
 * Class for an option that takes a single value.
 *
 * @tparam  the type of the converted option value
 *
 * @param names       sequence of names the option is known by
 * @param valueName   the placeholder name for the option's value, for the
 *                    usage message
 * @param description textual description of the option
 * @param convert     a function that will convert a string value for the
 *                    option to an appropriate value of type `T`.
 */
class SingleValueOption[T](val names: Seq[String],
                           val valueName: String,
                           val description: String,
                           val convert: Converter[T])
extends ArgotOption[T] with SingleValueArg[T] {
  require ((names != Nil) && (! names.exists(_.length == 0)))
}

object SingleValueOption {
  def apply[T](names: Seq[String], valueName: String, description: String)
              (implicit convert: Converter[T]): SingleValueOption[T] = {
    new SingleValueOption[T](names, valueName, description, convert)
  }

  def apply[T](name: String, valueName: String, description: String)
              (implicit convert: Converter[T]): SingleValueOption[T] = {
    apply(Seq(name), valueName, description)(convert)
  }
}

/**
 * Class for an option that takes a multiple values. Each instance of the
 * option on the command line adds to the sequence of values associated
 * with the option.
 *
 * @tparam  the type of the converted option value
 *
 * @param names       the list of names the option is known by
 * @param valueName   the placeholder name for the option's value, for the
 *                    usage message
 * @param description textual description of the option
 * @param convert     a function that will convert a string value for the
 *                    option to an appropriate value of type `T`.
 */
class MultiValueOption[T](val names: Seq[String],
                          val valueName: String,
                          val description: String,
                          val convert: Converter[T])
extends ArgotOption[T] with MultiValueArg[T] {
  require ((names != Nil) && (! names.exists(_.length == 0)))
}

object MultiValueOption {
  def apply[T](names: Seq[String], valueName: String, description: String)
              (implicit convert: Converter[T]): MultiValueOption[T] = {
    new MultiValueOption[T](names, valueName, description, convert)
  }

  def apply[T](name: String, valueName: String, description: String)
              (implicit convert: Converter[T]): MultiValueOption[T] = {
    apply(Seq(name), valueName, description)(convert)
  }
}

/**
 * Class for a flag. A flag option consists of a set of names that enable
 * the flag (e.g.,, set it to true) if present on the command line, and a set
 * of names that disable the flag (e.g., set it to false) if present on the
 * command line. The type of flag option can be anything, but is generally
 * boolean.
 *
 * @tparam  the underlying value type
 *
 * @param namesOn     list of names (short or long) that toggle the value on
 * @param namesOff    list of names (short or long) that toggle the value off
 * @param description textual description of the option
 * @param convert     a function that takes a boolean value and maps it to
 *                    the appropriate value to store as the option's value.
 *                    Takes the boolean value and the option string.
 */
class FlagOption[T](namesOn: Seq[String],
                    namesOff: Seq[String],
                    val description: String,
                    val convert: FlagConverter[T])
extends ArgotOption[T] {
  val supportsMultipleValues = false
  val hasValue: Boolean = true

  private val shortNamesOnSet = namesOn.filter(_.length == 1).toSet
  private val shortNamesOffSet = namesOff.filter(_.length == 1).toSet
  private val longNamesOnSet = namesOn.filter(_.length > 1).toSet
  private val longNamesOffSet = namesOff.filter(_.length > 1).toSet

  require (wellDefined)

  val names = namesOn.toList ::: namesOff.toList

  /** Displayable name for the argument, used in the usage message.
   *
   * @return the name
   */
  override def name = namesOn match {
    case c :: tail => "-" + c.toString
    case Nil       => "--" + namesOff(0)
  }

  /** Return an object that represents the key for this parameter, suitable
   * for hashing, sorting, etc. They key for a command line option is the
   * result of calling `name()`.
   *
   * @return the key
   */
  override protected def key =
    namesOn.mkString("|") + "!" + namesOff.mkString("|")

  private def wellDefined: Boolean = {
    def inBoth(s: String) = {
      (((shortNamesOnSet | longNamesOnSet) contains s) &&
       ((shortNamesOffSet | longNamesOffSet) contains s))
    }

    (names != Nil) && (! names.exists(_.length == 0)) && (! names.exists(inBoth _))
  }
}

object FlagOption {
  def apply[T](namesOn: Seq[String], namesOff: Seq[String], description: String)
              (implicit convert: FlagConverter[T]): FlagOption[T] = {
    new FlagOption[T](namesOn, namesOff, description, convert)
  }

  def apply[T](nameOn: String, nameOff: String, description: String)
              (implicit convert: FlagConverter[T]): FlagOption[T] = {
    apply(Seq(nameOn), Seq(nameOff), description)(convert)
  }

  def apply[T](nameOn: String, description: String)
              (implicit convert: FlagConverter[T]): FlagOption[T] = {
    apply(Seq(nameOn), Seq.empty[String], description)(convert)
  }
}

/**
 * Base trait for parameter classes
 */
private[argot] trait Parameter[T]
extends Argument[T] with HasValue[T] {
  val convert: Converter[T]
  val description: String
  val optional: Boolean

  require (valueName.length > 0)

  def name = valueName


  /** Retrieve a parsed value of the parameter.
    *
    * @param parsed  The parsed argument object
    *
    * @return `None`, if the parameter doesn't exist at all.
    *         `Some(Left(error))` if there was an error parsing the parameter.
    *         `Some(Right(value))` if a legal value was parsed
    */
  def get(p: ParsedParameters): Option[Either[String, T]] = {
    p.parameters.get(this).map(e => e match {
      case Right(value)  => Right[String, T](value.asInstanceOf[T])
      case Left(message) => Left[String, T](message)
    })
  }

  override def toString = "parameter " + valueName
  protected def key = valueName
}

/**
 * Class for a non-option parameter that takes a single value.
 *
 * @tparam  the type of the converted parameter value
 *
 * @param valueName    the placeholder name for the parameter's value,
 *                     for the usage message
 * @param description  textual description of the parameter
 * @param optional     whether or not the parameter is optional. Only one
 *                     parameter may be optional, and it must be last one
 * @param convert      a function that will convert a string value for
   *                     the parameter to an appropriate value of type `T`.
 */
class SingleValueParameter[T](val valueName: String,
                              val description: String,
                              val optional: Boolean,
                              val convert: Converter[T])
extends Parameter[T] with SingleValueArg[T]

object SingleValueParameter {
  def apply[T](valueName: String, description: String, optional: Boolean)
              (implicit convert: Converter[T]): SingleValueParameter[T] = {
    new SingleValueParameter[T](valueName, description, optional, convert)
  }
}

/**
 * Class for a non-option parameter that takes a multiple values. Each
 * instance of the parameter on the command line adds to the sequence of
 * values associated with the parameter.
 *
 * @tparam  the type of the converted parameter value
 *
 * @param valueName    the placeholder name for the parameter's value,
 *                     for the usage message
 * @param description  textual description of the parameter
 * @param optional     whether or not the parameter is optional. Only one
 *                     parameter may be optional, and it must be the last one.
 * @param convert      a function that will convert a string value for
   *                   the parameter to an appropriate value of type `T`.
 */
class MultiValueParameter[T](val valueName: String,
                             val description: String,
                             val optional: Boolean,
                             val convert: Converter[T])
extends Parameter[T] with MultiValueArg[T]

object MultiValueParameter {
  def apply[T](valueName: String, description: String, optional: Boolean)
              (implicit convert: Converter[T]): MultiValueParameter[T] = {
    new MultiValueParameter[T](valueName, description, optional, convert)
  }
}

/**
 * Internally used common conversion functions
 */
private object Conversions {
  implicit def parseInt(s: String): Either[String, Int] = {
    parseNum[Int](s, s.toInt)
  }

  implicit def parseLong(s: String): Either[String, Long] = {
    parseNum[Long](s, s.toLong)
  }

  implicit def parseShort(s: String): Either[String, Short] = {
    parseNum[Short](s, s.toShort)
  }

  implicit def parseFloat(s: String): Either[String, Float] = {
    parseNum[Float](s, s.toFloat)
  }

  implicit def parseDouble(s: String): Either[String, Double] = {
    parseNum[Double](s, s.toDouble)
  }

  implicit def parseChar(s: String): Either[String, Char] = {
    if (s.length != 1)
      Left("Cannot parse \"" + s + "\" to a character.")
    else
      Right(s(0))
  }

  implicit def parseByte(s: String): Either[String, Byte] = {
    val num = s.toInt
    if ((num < 0) || (num > 255))
      Left("\"%s\" results in a number that is too large for a byte".format(s))
    else
      Right(num.toByte)
  }

  implicit def parseString(s: String, opt: String): Either[String, String] = {
    Right(s)
  }

  private def parseNum[T](s: String, parse: => T): Either[String, T] = {
    try {
      Right(parse)
    }

    catch {
      case e: NumberFormatException =>
        Left("Cannot convert argument \"" + s + "\" to a number.")
    }
  }
}

import scala.collection.immutable.ListMap

/** A container for parsed arguments.
  */
case class ParsedParameters(options: Map[ArgotOption[_], Either[String, _]],
                            parameters: Map[Parameter[_], Either[String, _]])

/** Specification for an argument parser.
  */
class ArgotSpecification(val programName: String,
                         val options: Seq[ArgotOption[_]],
                         val parameters: Seq[Parameter[_]]) {
  private val optionMap = Map(options.map(o => o.name -> o): _*)
  private val paramMap  = ListMap(parameters.map(p => p.name -> p): _*)

  override def toString = {
    "ArgotSpecification<programName=%s, options=%s, parameters=%s>".format(
      programName, options.toString, parameters.toString
    )
  }
}

/** Mutable class used to build an immutable ArgotParser.
  */
class ArgotParser (
  val         programName:  String,
  private val shortNameMap: Map[Char, ArgotOption[_]] = Map.empty[Char, ArgotOption[_]],
  private val longNameMap:  Map[String, ArgotOption[_]]= Map.empty[String, ArgotOption[_]],
  private val allOptions:   ListMap[String, ArgotOption[_]] = ListMap.empty[String, ArgotOption[_]],
  private val nonFlags:     ListMap[String, HasValue[_]] = ListMap.empty[String, HasValue[_]],
  private val flags:        ListMap[String, FlagOption[_]] = ListMap.empty[String, FlagOption[_]],
  private val parameters:   ListMap[String, Parameter[_]] = ListMap.empty[String, Parameter[_]],
  private val preUsage:     Option[String] = None,
  private val postUsage:    Option[String] = None
) {

  private def newOption[T](opt: ArgotOption[T]): (Map[Char, ArgotOption[_]], 
                                                  Map[String, ArgotOption[_]], 
                                                  ListMap[String, ArgotOption[_]]) = {
    val newShort: Map[Char, ArgotOption[_]] = (
      shortNameMap ++ 
      opt.names.filter(_.length == 1).map((n: String) => n(0) -> opt).toMap
    )

    val newLong: Map[String, ArgotOption[_]] = (
      longNameMap ++
      opt.names.filter(_.length > 1).map(n => n -> opt).toMap
    )

    val newAll: ListMap[String, ArgotOption[_]] = (
      allOptions ++ Seq(opt.name -> opt)
    )

    (newShort, newLong, newAll)
  }

  private def copy[T](opt: ArgotOption[T] with HasValue[T]): ArgotParser = {
    val newNonFlags = nonFlags + (opt.name -> opt)
    val (newShort, newLong, newAll) = newOption(opt)
    new ArgotParser(
      programName, newShort, newLong, newAll, newNonFlags, flags, parameters
    )
  }

  private def copy[T](flag: FlagOption[T]): ArgotParser = {

    val (newShort, newLong, newAll) = newOption(flag)
    val newFlags = flags + (flag.name -> flag)

    new ArgotParser(
      programName,
      shortNameMap = newShort,
      longNameMap = newLong,
      allOptions = newAll,
      flags = newFlags
    )
  }
 
  def copy(param: Parameter[_]): ArgotParser = {
    new ArgotParser(programName, parameters = parameters + (param.name -> param))
  }
 
  /** Define an option that takes a single value of type `T`.
   *
   * @tparam T  the type of the option's value, which will be stored in
   *            the `SingleValueOption` object's `value` field.
   *
   * @param names       the list of names for the option. Each name can be
   *                    a single character (thus, "v" corresponds to "-v") or
   *                    multiple characters ("verbose" for "--verbose").
   * @param valueName   a name to use for the associated value in the
   *                    generated usage message
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a string value into
   *                    type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For common types, the implicit functions in the
   *                    `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def option[T](names: Seq[String], valueName: String, description: String)
               (implicit convert: (String, SingleValueOption[T]) => T):
    ArgotParser = {
    names.foreach(checkOptionName)
    copy(new SingleValueOption[T](this, names, valueName, description, convert))
  }

  /** Define an option that takes a single value of type `T`. This short-hand
   * method provides only one option name, as opposed to a list of option
   * names.
   *
   * @tparam T  the type of the option's value, which will be stored in
   *            the `SingleValueOption` object's `value` field.
   *
   * @param name        the name for the option. The name can be a single
   *                    character (thus, "v" corresponds to "-v") or
   *                    multiple characters ("verbose" for "--verbose").
   * @param valueName   a name to use for the associated value in the
   *                    generated usage message
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a string value into
   *                    type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For common types, the implicit functions in the
   *                    `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def option[T](name: String, valueName: String, description: String)
               (implicit convert: (String, SingleValueOption[T]) => T):
    ArgotParser = {
    option[T](List(name), valueName, description)(convert)
  }

  /** Define an option that takes a sequence of values of type `T`. Each
   * invocation of the option on the command line contributes a new value
   * to the sequence.
   *
   * @tparam T  the type of the option's value, which added to the
   *            the `MultiValueOption` object's `value` sequence field.
   *
   * @param names       the list of names for the option. Each name can be
   *                    a single character (thus, "v" corresponds to "-v") or
   *                    multiple characters ("verbose" for "--verbose").
   * @param valueName   a name to use for the associated value in the
   *                    generated usage message
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a string value into
   *                    type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For common types, the implicit functions in the
   *                    `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def multiOption[T](names: Seq[String],
                     valueName: String,
                     description: String)
                    (implicit convert: (String, MultiValueOption[T]) => T):
    ArgotParser = {
    names.foreach(checkOptionName)
    copy(new MultiValueOption[T](this, names, valueName, description, convert))
  }

  /** Define an option that takes a sequence of values of type `T`. Each
   * invocation of the option on the command line contributes a new value
   * to the sequence. This short-hand method provides only one option
   * name, as opposed to a list of option names.
   *
   * @tparam T  the type of the option's value, which added to the
   *            the `MultiValueOption` object's `value` sequence field.
   *
   * @param name        the name for the option. The name can be a single
   *                    character (thus, "v" corresponds to "-v") or
   *                    multiple characters ("verbose" for "--verbose").
   * @param valueName   a name to use for the associated value in the
   *                    generated usage message
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a string value into
   *                    type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For common types, the implicit functions in the
   *                    `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def multiOption[T](name: String, valueName: String, description: String)
                    (implicit convert: (String, MultiValueOption[T]) => T):
    ArgotParser = {
    multiOption[T](List(name), valueName, description)(convert)
  }

  /** Define a flag option. Flag options take no value parameters.
   * Instead, a flag option is simply present or absent. Typically, flag
   * options are associated with boolean value, though Argot will permit
   * you to associate them with any type you choose.
   *
   * Flag options permit you to segregate the option names into ''on''
   * names and ''off'' names. With boolean flag options, the ''on'' names
   * set the value to `true`, and the ''off'' names set the value to
   * values. With typed flag options, what happens depends on the
   * conversion function. Whatever the conversion function returns gets
   * stored as the option's value.
   *
   * Flag conversion functions receive a boolean parameter, indicating
   * whether an ''on'' option was seen (`true`) or an ''off'' option was
   * seen (`false`). The built-in conversion functions for boolean flags
   * simply return the value of the boolean, which is then stored in the
   * (Boolean) flag option's value. However, it's perfectly reasonable
   * to have flag options with other types. For instance, one could easily
   * define a "Verbosity" flag option of type `Int`, where each ''on''
   * option increments the verbosity level and each ''off'' option decrements
   * the value.
   *
   * @tparam T  the type of the option's value, which will be stored in
   *            the `FlagOption` object's `value` field.
   *
   * @param namesOn     the names for the option that enable (turn on) the
   *                    option. Each name can be a single
   *                    character (thus, "v" corresponds to "-v") or
   *                    multiple characters ("verbose" for "--verbose").
   * @param namesOff    the names for the option that disable (turn off) the
   *                    option. Each name can be a single
   *                    character (thus, "v" corresponds to "-v") or
   *                    multiple characters ("verbose" for "--verbose").
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a boolean on/off value
   *                    to type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For a boolean flag option , the implicit functions in
   *                    the `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def flag[T](namesOn: Seq[String], namesOff: Seq[String], description: String)
             (implicit convert: (Boolean, FlagOption[T]) => T):
    ArgotParser = {
    namesOn.foreach(checkOptionName)
    namesOff.foreach(checkOptionName)
    copy(new FlagOption[T](this, namesOn, namesOff, description, convert))
  }

  /** Define a flag option. Flag options take no value parameters.
   * Instead, a flag option is simply present or absent. Typically, flag
   * options are associated with boolean value, though Argot will permit
   * you to associate them with any type you choose.
   *
   * Flag options permit you to segregate the option names into ''on''
   * names and ''off'' names. With boolean flag options, the ''on'' names
   * set the value to `true`, and the ''off'' names set the value to
   * values. With typed flag options, what happens depends on the
   * conversion function. Whatever the conversion function returns gets
   * stored as the option's value.
   *
   * Flag conversion functions receive a boolean parameter, indicating
   * whether an ''on'' option was seen (`true`) or an ''off'' option was
   * seen (`false`). The built-in conversion functions for boolean flags
   * simply return the value of the boolean, which is then stored in the
   * (Boolean) flag option's value. However, it's perfectly reasonable
   * to have flag options with other types. For instance, one could easily
   * define a "Verbosity" flag option of type `Int`, where each ''on''
   * option increments the verbosity level and each ''off'' option decrements
   * the value.
   *
   * @tparam T  the type of the option's value, which will be stored in
   *            the `FlagOption` object's `value` field.
   *
   * @param namesOn     the names for the option that enable (turn on) the
   *                    option. Each name can be a single character (thus,
   *                    "v" corresponds to "-v") or multiple characters
   *                    ("verbose" for "--verbose").
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a boolean on/off value
   *                    to type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For a boolean flag option , the implicit functions in
   *                    the `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def flag[T](namesOn: Seq[String], description: String)
            (implicit convert: (Boolean, FlagOption[T]) => T):
    ArgotParser = {
    flag(namesOn, Nil, description)(convert)
  }

  /** Define a flag option. Flag options take no value parameters.
   * Instead, a flag option is simply present or absent. Typically, flag
   * options are associated with boolean value, though Argot will permit
   * you to associate them with any type you choose.
   *
   * Flag options permit you to segregate the option names into ''on''
   * names and ''off'' names. With boolean flag options, the ''on'' names
   * set the value to `true`, and the ''off'' names set the value to
   * values. With typed flag options, what happens depends on the
   * conversion function. Whatever the conversion function returns gets
   * stored as the option's value.
   *
   * Flag conversion functions receive a boolean parameter, indicating
   * whether an ''on'' option was seen (`true`) or an ''off'' option was
   * seen (`false`). The built-in conversion functions for boolean flags
   * simply return the value of the boolean, which is then stored in the
   * (Boolean) flag option's value. However, it's perfectly reasonable
   * to have flag options with other types. For instance, one could easily
   * define a "Verbosity" flag option of type `Int`, where each ''on''
   * option increments the verbosity level and each ''off'' option decrements
   * the value.
   *
   * @tparam T  the type of the option's value, which will be stored in
   *            the `FlagOption` object's `value` field.
   *
   * @param name        the name for the option that enables (turns on) the
   *                    option. The name can be a single character (thus,
   *                    "v" corresponds to "-v") or multiple characters
   *                    ("verbose" for "--verbose").
   * @param description a description for the option, for the usage message
   * @param convert     a function that will convert a boolean on/off value
   *                    to type `T`. The function should throw
   *                    `ArgotConversionException` on conversion error.
   *                    For a boolean flag option , the implicit functions in
   *                    the `ArgotConverters` module are often suitable.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified option
   */
  def flag[T](name: String, default: T, description: String)
             (implicit convert: (Boolean, FlagOption[T]) => T):
    ArgotParser = {
    flag[T](List(name), description)(convert)
  }

  /** Define a positional parameter that has a single value. Positional
   * parameters are parsed from the command line in the order they are
   * added to the `ArgotParser`. See the class documentation for complete
   * details.
   *
   * @tparam  the type of the converted parameter value
   *
   * @param valueName    the placeholder name for the parameter's value,
   *                     for the usage message
   * @param description  textual description of the parameter
   * @param optional     whether or not the parameter is optional. Only one
   *                     parameter may be optional, and it must be the last
   *                     one.
   * @param convert      a function that will convert a string value for
   *                     the parameter to an appropriate value of type `T`.
   *                     The function should throw `ArgotConversionException`
   *                     on conversion error.
   *
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified parameter
   */
  def parameter[T](valueName: String, description: String, optional: Boolean)
                  (implicit convert: (String, Parameter[T]) => T):
    ArgotParser = {
    val param = new SingleValueParameter[T](this,
                                            valueName,
                                            description,
                                            optional,
                                            convert)
    checkOptionalStatus(param, optional)
    checkForMultiParam(param)
    copy(param)
  }

  /** Define a positional parameter that can occur multiple times. Only
   * one such parameter can exist, and it must be the last parameter
   * in the command line. See the class documentation for complete
   * details.
   *
   * @tparam  the type of the converted parameter value
   *
   * @param valueName    the placeholder name for the parameter's value,
   *                     for the usage message
   * @param description  textual description of the parameter
   * @param optional     whether or not the parameter is optional. Only one
   *                     parameter may be optional, and it must be the last
   *                     one.
   * @param convert      a function that will convert a string value for
   *                     the parameter to an appropriate value of type `T`.
   *                     The function should throw `ArgotConversionException`
   *                     on conversion error.
   *
   * @return a new copy of the `ArgotParser` object, incorporating the
   *         specified parameter
   */
  def multiParameter[T](valueName: String,
                        description: String,
                        optional: Boolean)
                       (implicit convert: (String, Parameter[T]) => T):
    ArgotParser = {
    val param = new MultiValueParameter[T](this,
                                           valueName,
                                           description,
                                           optional,
                                           convert)
    checkOptionalStatus(param, optional)
    checkForMultiParam(param)
    copy(param)
  }

  private def checkOptionName(name: String) = {
    name.toList match {
      case '-' :: tail =>
        throw new ArgotSpecificationError(
          "Option name \"" + name + "\" must not start with \"-\"."
        )
      case Nil =>
        throw new ArgotSpecificationError("Empty option name.")

      case _ =>
    }
  }

  private def checkForMultiParam(param: Parameter[_]) = {
    if (parameters.size > 0) {
      parameters last match {
        case p: MultiValueParameter[_] =>
          throw new ArgotSpecificationError(
            "Multi-parameter \"" + p.name + "\" must be the last " +
            "parameter in the specification."
          )

        case _ =>
      }
    }
  }

  private def checkOptionalStatus(param: Parameter[_],
                                  optionalSpec: Boolean) = {
    if (parameters.size > 0) {
      val last = parameters.last._2
      if (last.optional && (! optionalSpec)) {
        throw new ArgotSpecificationError(
          ("You can't follow optional parameter \"%s\" with " +
           "required parameter \"%s\"").format(last.name, param.valueName))
      }
    }
  }
}

object ArgotParser {
  def apply() {

  }
}