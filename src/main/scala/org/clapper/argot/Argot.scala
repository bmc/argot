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

/** Base trait for all option and parameter classes, `ArgotArgument`
  * contains comment methods and values.
  *
  * @tparam T  the type associated with the argument
  */
trait ArgotArgument[T] {

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
      case that: ArgotArgument[_] =>
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
 * support one or mor associated values of type `T`.
 *
 * @tparam T  the value type
 *
 * @see SingleValueArg
 * @see MultiValueArg
 */
trait HasValue[T] extends ArgotArgument[T] {
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

  /** Method that converts a string value to type `T`. Should throw
   * `ArgotConversionException` on error.
   *
   * @param s  the string to convert
   *
   * @return the converted result
   *
   * @throws ArgotConversionException  conversion error
   */
  def convertString(s: String): T
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

  def reset() = optValue = None

  /** Get the option's value.
   *
   * @return `Some(value)` if the value is set; `None` if not.
   */
  def value: Option[T] = optValue

  private[argot] def storeValue(v: T) = optValue = Some(v)
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
trait ArgotOption[T] extends ArgotArgument[T] {
  /** List of option names, both long (multi-character) and short
   * (single-character).
   */
  val names: List[String]

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
 * @param parent      the parent parser instance that owns the option
 * @param names       the list of names the option is known by
 * @param valueName   the placeholder name for the option's value, for the
 *                    usage message
 * @param description textual description of the option
 * @param convert     a function that will convert a string value for the
 *                    option to an appropriate value of type `T`.
 */
class SingleValueOption[T](val parent: ArgotParser,
                           val names: List[String],
                           val valueName: String,
                           val description: String,
                           val convert: (String, SingleValueOption[T]) => T)
extends ArgotOption[T] with SingleValueArg[T] {
  require ((names != Nil) && (! names.exists(_.length == 0)))

  def convertString(s: String): T = convert(s, this)
}

/**
 * Class for an option that takes a multiple values. Each instance of the
 * option on the command line adds to the sequence of values associated
 * with the option.
 *
 * @tparam  the type of the converted option value
 *
 * @param parent      the parent parser instance that owns the option
 * @param names       the list of names the option is known by
 * @param valueName   the placeholder name for the option's value, for the
 *                    usage message
 * @param description textual description of the option
 * @param convert     a function that will convert a string value for the
 *                    option to an appropriate value of type `T`.
 */
class MultiValueOption[T](val parent: ArgotParser,
                          val names: List[String],
                          val valueName: String,
                          val description: String,
                          val convert: (String, MultiValueOption[T]) => T)
extends ArgotOption[T] with MultiValueArg[T] {
  require ((names != Nil) && (! names.exists(_.length == 0)))

  def convertString(s: String): T = convert(s, this)
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
 * @param parent      the parent parser instance that owns the option
 * @param namesOn     list of names (short or long) that toggle the value on
 * @param namesOff    list of names (short or long) that toggle the value off
 * @param description textual description of the option
 * @param convert     a function that takes a boolean value and maps it to
 *                    the appropriate value to store as the option's value.
 */
class FlagOption[T](val parent: ArgotParser,
                    namesOn: List[String],
                    namesOff: List[String],
                    val description: String,
                    val convert: (Boolean, FlagOption[T]) => T)
extends ArgotOption[T] {
  val supportsMultipleValues = false
  val hasValue: Boolean = true

  private val shortNamesOnSet = namesOn.filter(_.length == 1).toSet
  private val shortNamesOffSet = namesOff.filter(_.length == 1).toSet
  private val longNamesOnSet = namesOn.filter(_.length > 1).toSet
  private val longNamesOffSet = namesOff.filter(_.length > 1).toSet

  require (wellDefined)

  val names = namesOn ::: namesOff

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
    def inBoth(s: String) = {}
      (((shortNamesOnSet | longNamesOnSet) contains s) &&
       ((shortNamesOffSet | longNamesOffSet) contains s))
    }

    val l = namesOn ::: namesOff
    (l != Nil) && (! l.exists(_.length == 0)) && (! l.exists(inBoth _))
  }

  private def checkValidity(optName: String) = {
    if (! ((shortNamesOnSet contains optName) ||
           (shortNamesOffSet contains optName) ||
           (longNamesOnSet contains optName) ||
           (longNamesOffSet contains optName)) )
      throw new ArgotException("(BUG) Flag name \"" + optName +
                               "\" is neither a short nor a long name " +
                               "for option \"" + this.name + "\"")
  }
}

/**
 * Base trait for parameter classes
 */
private[argot] trait Parameter[T]
extends ArgotArgument[T] with HasValue[T] {
  val convert: (String, Parameter[T]) => T
  val description: String
  val optional: Boolean

  require (valueName.length > 0)

  def name = valueName
  def convertString(s: String): T = convert(s, this)
  override def toString = "parameter " + valueName
  protected def key = valueName
}

/**
 * Class for a non-option parameter that takes a single value.
 *
 * @tparam  the type of the converted parameter value
 *
 * @param parent       the parent parser instance that owns the parameter
 * @param valueName    the placeholder name for the parameter's value,
 *                     for the usage message
 * @param description  textual description of the parameter
 * @param optional     whether or not the parameter is optional. Only one
 *                     parameter may be optional, and it must be last one
 * @param convert      a function that will convert a string value for
   *                     the parameter to an appropriate value of type `T`.
 */
class SingleValueParameter[T](
  val parent: ArgotParser,
  val valueName: String,
  val description: String,
  val optional: Boolean,
  val convert: (String, Parameter[T]) => T)
extends Parameter[T] with SingleValueArg[T]

/**
 * Class for a non-option parameter that takes a multiple values. Each
 * instance of the parameter on the command line adds to the sequence of
 * values associated with the parameter.
 *
 * @tparam  the type of the converted parameter value
 *
 * @param parent       the parent parser instance that owns the parameter
 * @param valueName    the placeholder name for the parameter's value,
 *                     for the usage message
 * @param description  textual description of the parameter
 * @param optional     whether or not the parameter is optional. Only one
 *                     parameter may be optional, and it must be the last one.
 * @param convert      a function that will convert a string value for
   *                     the parameter to an appropriate value of type `T`.
 */
class MultiValueParameter[T](
  val parent: ArgotParser,
  val valueName: String,
  val description: String,
  val optional: Boolean,
  val convert: (String, Parameter[T]) => T)
extends Parameter[T] with MultiValueArg[T]

/**
 * Internally used common conversion functions
 */
private object Conversions {
  implicit def parseInt(s: String, opt: String): Int = {
    parseNum[Int](s, s.toInt)
  }

  implicit def parseLong(s: String, opt: String): Long = {
    parseNum[Long](s, s.toLong)
  }

  implicit def parseShort(s: String, opt: String): Short = {
    parseNum[Short](s, s.toShort)
  }

  implicit def parseFloat(s: String, opt: String): Float = {
    parseNum[Float](s, s.toFloat)
  }

  implicit def parseDouble(s: String, opt: String): Double = {
    parseNum[Double](s, s.toDouble)
  }

  implicit def parseChar(s: String, opt: String): Char = {
    if (s.length != 1)
      throw new ArgotConversionException(
        "Option \"" + opt + "\": " +
        "Cannot parse \"" + s + "\" to a character."
      )
    s(0)
  }

  implicit def parseByte(s: String, opt: String): Byte = {
    val num = s.toInt
    if ((num < 0) || (num > 255))
      throw new ArgotConversionException(
        "Option \"" + opt + "\": " + "\"" + s +
        "\" results in a number that is too large for a byte."
      )

    num.toByte
  }

  implicit def parseString(s: String, opt: String): String = {
    s
  }

  private def parseNum[T](s: String, parse: => T): T = {
    try {
      parse
    }

    catch {
      case e: NumberFormatException =>
        throw new ArgotConversionException(
          "Cannot convert argument \"" + s + "\" to a number."
        )
    }
  }
}

/**
 * `ArgotParser` is a command-line parser, with support for single-value and
 * multi-value options, single-value and multi-value parameters, typed value,
 * custom conversions (with suitable defaults), and extensibility.
 *
 * An `ArgotParser` embodies a representation of the command line: its
 * expected options and their value types, the expected positional
 * parameters and their value types, the name of the program, and other
 * values.
 *
 * For complete details, see the Argot web site (linked below).
 *
 * @param programName  the name of the program, for the usage message
 * @param compactUsage force a more compact usage message
 * @param outputWidth  width of the output; used when wrapping the usage
 *                     message
 * @param preUsage     optional message to issue before the usage message
 *                     (e.g., a copyright and/or version string)
 * @param postUsage    optional message to issue after the usage message
 * @param sortUsage    If `true` (the default), the options are sorted
 *                     alphabetically in the usage output. If `false`, they
 *                     are displayed in the order they were created.
 *
 * @see <a href="http://software.clapper.org/argot/" target="argot">the Argot web site</a>
 */
class ArgotParserOld(programName: String,
                  compactUsage: Boolean = false,
                  outputWidth: Int = 79,
                  preUsage: Option[String] = None,
                  postUsage: Option[String] = None,
                  sortUsage: Boolean = true) {
  require(outputWidth > 0)

  import scala.collection.mutable.{Map => MutableMap, LinkedHashSet, LinkedHashMap}
  protected val shortNameMap = MutableMap.empty[Char, ArgotOption[_]]
  protected val longNameMap = MutableMap.empty[String, ArgotOption[_]]
  protected val allOptions = new LinkedHashMap[String, ArgotOption[_]]
  protected val nonFlags = new LinkedHashSet[HasValue[_]]
  protected val flags = new LinkedHashSet[FlagOption[_]]
  protected val parameters = new LinkedHashSet[Parameter[_]]


  /** Parse the specified array of command-line arguments, according to the
   * parser's specification. A successful parse sets the various value
   * objects returned by the specification methods.
   *
   * @param args the command line parameters
   *
   * @throws ArgotUsageException  user error on the command line; the
   *                              exception contains the usage message
   * @throws ArgotException       some other kind of fatal error
   */
  def parse(args: Array[String]): Unit = parse(args.toList)

  /** Parse the specified array of command-line arguments, according to the
   * parser's specification. A successful parse sets the various value
   * objects returned by the specification methods.
   *
   * @param args the command line parameters
   *
   * @throws ArgotUsageException  user error on the command line; the
   *                              exception contains the usage message
   * @throws ArgotException       some other kind of fatal error
   */
  def parse(args: List[String]): Unit = {
    try {
      parseArgList(args)
    }

    catch {
      case e: ArgotConversionException => usage(e.message)
    }
  }

  /** Generate the usage string. This string is automatically included in
   * any thrown `ArgotUsageException`.
   *
   * @param message  optional message to prefix the usage string.
   *
   * @return the usage string, wrapped appropriately.
   */
  def usageString(message: Option[String] = None): String = {
    import grizzled.math.{util => MathUtil}
    import grizzled.string.WordWrapper

    val SPACES_BETWEEN = 2  // spaces between arg name and description

    def paramString(p: Parameter[_]): String =
      if (p.optional) "[" + p.name + "]" else p.name

    def optString(name: String, opt: ArgotOption[_]) = {
      val hyphen = if (name.length == 1) "-" else "--"

      opt match {
        case ov: HasValue[_] => hyphen + name + " " + ov.valueName
        case _               => hyphen + name
      }
    }

    val mmax = MathUtil.max _

    // Calculate the maximum length of all the option strings.

    val maxOptLen = mmax( {
      for {opt <- allOptions.values
           name <- opt.names}
        yield optString(name, opt).length
    }.toSeq: _*)

    // Create the output buffer.

    val buf = new StringBuilder

    // Build the usage line.
    val wrapper = new WordWrapper(wrapWidth=outputWidth)

    message.foreach(s => buf.append(wrapper.wrap(s) + "\n\n"))
    preUsage.foreach(s => buf.append(wrapper.wrap(s) + "\n\n"))
    buf.append("Usage: " + programName)
    if (allOptions.size > 0)
      buf.append(" [OPTIONS]")

    for (p <- parameters) {
      buf.append(" ")
      buf.append(
        p match {
          case _: SingleValueParameter[_] => paramString(p)
          case _: MultiValueParameter[_]  => paramString(p) + " ..."
        }
      )
    }
    
    buf.append('\n')

    // Build the option summary.

    def handleOneOption(key: String) = {
      if (! compactUsage)
        buf.append("\n")

      val opt = allOptions(key)

      // Ensure that the short names always appear before the long
      // names.

      val sorted = opt.names.filter(_.length == 1).sortWith(_ < _) :::
      opt.names.filter(_.length > 1).sortWith(_ < _)

      for (name <- sorted.take(sorted.length - 1))
        buf.append(optString(name, opt) + "\n")

      val name = sorted.takeRight(1)(0)
      val os = optString(name, opt)
      val padding = (maxOptLen - os.length) + SPACES_BETWEEN
      val prefix = os + (" " * padding)
      val wrapper = new WordWrapper(prefix=prefix,
                                    wrapWidth=outputWidth)
      val desc = opt match {
        case o: HasValue[_] =>
          if (o.supportsMultipleValues)
            o.description + " (May be specified multiple times.)"
          else
            o.description

        case _ =>
          opt.description
        
      }

      buf.append(wrapper.wrap(desc))
      buf.append("\n")
    }

    def handleOneParameter(p: Parameter[_], maxNameLen: Int) = {
      if (! compactUsage)
        buf.append('\n')

      val padding = (maxNameLen - p.name.length) + SPACES_BETWEEN
      val prefix = p.name + (" " * padding)
      val wrapper = new WordWrapper(prefix=prefix, wrapWidth=outputWidth)
      val desc = p match {
        case o: HasValue[_] =>
          if (o.supportsMultipleValues)
            o.description + " (May be specified multiple times.)"
          else
            o.description

        case _ =>
          p.description
      }

      buf.append(wrapper.wrap(desc))
      buf.append('\n')
    }

    if (allOptions.size > 0) {
      buf.append("\nOPTIONS\n")
      val optionKeys =
        if (sortUsage)
          allOptions.keySet.toList.sortWith(_ < _)
        else
          allOptions.keySet.toList

      optionKeys.foreach(handleOneOption)
    }

    if (parameters.size > 0) {
      buf.append("\nPARAMETERS\n")
      val maxNameLen = mmax(parameters.map(_.name.length).toList: _*)
      parameters.toList.foreach(handleOneParameter(_, maxNameLen))
    }

    postUsage.foreach(s => buf.append(wrapper.wrap(s) + "\n"))
    buf.toString
  }

  /** Throws an `ArgotUsageException` containing the usage string.
   *
   * @throws ArgotUsageException  unconditionally
   */
  def usage() = throw new ArgotUsageException(usageString())

  /** Throws an `ArgotUsageException` containing the usage string.
   *
   * @param message  optional message to prefix the usage string.
   *
   * @throws ArgotUsageException  unconditionally
   */
  def usage(message: String) =
    throw new ArgotUsageException(usageString(Some(message)))

  // -----------------------------------------------------------------------
  // Private Methods
  // -----------------------------------------------------------------------

  private def replaceOption(opt: ArgotOption[_]) {
    opt.names.filter(_.length == 1).
    foreach(s => shortNameMap += s(0) -> opt)
    opt.names.filter(_.length > 1).foreach(s => longNameMap += s -> opt)
    allOptions += opt.name -> opt
  }

  private def replaceParameter(param: Parameter[_]) {
    parameters += param
  }

  private def parseParams(a: List[String]): Unit = {
    def parseNext(a: List[String], paramSpecs: List[Parameter[_]]):
    List[String] = {
      def checkMissing(paramSpecs: List[Parameter[_]]): List[String] = {
        if (paramSpecs.count(! _.optional) > 0)
          usage("Missing parameter(s): " +
                paramSpecs.filter(! _.optional).
                map(_.name).
                mkString(", ")
              )
        Nil
      }

      paramSpecs match {
        case Nil if (a.length > 0) =>
          usage("Too many parameters.")
        Nil

        case Nil =>
          Nil

        case (p: MultiValueParameter[_]) :: tail => {
          if (a.length == 0)
            checkMissing(paramSpecs)
          else
            a.foreach(s => p.setFromString(s))

          parseNext(Nil, tail)
        }

        case (p: SingleValueParameter[_]) :: tail => {
          if (a.length == 0)
            checkMissing(paramSpecs)
          else
            p.setFromString(a.take(1)(0))

          parseNext(a drop 1, tail)
        }
      }
    }

    parseNext(a, parameters.toList)
  }

  private def paddedList(l: List[String], total: Int): List[String] = {
    if (l.length >= total)
      l
    else
      l ::: (1 to (total - l.length)).map(i => "").toList
  }

  private def parseCompressedShortOpt(optString: String,
                                      optName: String,
                                      a: List[String]):
  List[String] = {
    assert(optName.length > 1)
    val (name, rest) = (optName take 1, optName drop 1)
      assert(rest.length > 0)
    val opt = shortNameMap.getOrElse(
      name(0), usage("Unknown option: " + optString)
    )

    val result = opt match {
      case o: HasValue[_] =>
        if (rest.length == 0)
          usage("Option -" + name + " requires a value.")
      o.setFromString(rest)
      a drop 1

      case o: FlagOption[_] =>
        // It's a flag. thus, the remainder of the option string
        // consists of the next set of options (e.g., -cvf)
        o.setByName(name)
      List("-" + rest) ::: (a drop 1)

      case _ =>
        throw new ArgotException("(BUG) Found " + opt.getClass +
                                 " in shortNameMap")
    }

    result
  }

  private def parseRegularShortOpt(optString: String,
                                   optName: String,
                                   a: List[String]):
  List[String] = {
    assert(optName.length == 1)
    val opt = shortNameMap.getOrElse(
      optName(0), usage("Unknown option: " + optString)
    )

    val a2 = a drop 1

    val result = opt match {
      case o: HasValue[_] =>
        if (a2.length == 0)
          usage("Option " + optString + " requires a value.")
      o.setFromString(a2(0))
      a2 drop 1

      case o: FlagOption[_] =>
        o.setByName(optName)
      a2

      case _ =>
        throw new ArgotException("(BUG) Found " + opt.getClass +
                                 " in longNameMap")
    }

    result
  }

  private def parseShortOpt(a: List[String]): List[String] = {
    val optString = a.take(1)(0)
    assert(optString startsWith "-")
    val optName = optString drop 1

    optName.length match {
      case 0 => usage("Missing option name in \"" + optString + "\"")
      case 1 => parseRegularShortOpt(optString, optName, a)
      case _ => parseCompressedShortOpt(optString, optName, a)
    }
  }

  def parseLongOpt(a: List[String]): List[String] = {
    val optString = a.take(1)(0)
    assert(optString startsWith "--")
    val optName = optString drop 2
    val opt = longNameMap.getOrElse(
      optName, usage("Unknown option: " + optString)
    )

    val a2 = a drop 1

    val result = opt match {
      case o: HasValue[_] =>
        if (a2.length == 0)
          usage("Option " + optString + " requires a value.")
      o.setFromString(a2(0))
      a2 drop 1

      case o: FlagOption[_] =>
        o.setByName(optName)
      a2

      case _ =>
        throw new ArgotException("(BUG) Found " + opt.getClass +
                                 " in longNameMap")
    }

    result
  }

  @tailrec private def parseArgList(a: List[String]): Unit = {
    a match {
      case Nil =>
        parseParams(Nil)

      case "--" :: tail =>
        parseParams(tail)

      case opt :: tail if (opt.startsWith("--")) =>
        parseArgList(parseLongOpt(a))

      case opt :: tail if (opt(0) == '-') =>
        parseArgList(parseShortOpt(a))

      case _ =>
        parseParams(a)
    }
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
      if (parameters.last.optional && (! optionalSpec))
        throw new ArgotSpecificationError(
          "Optional parameter \"" + parameters.last.name +
          "\" cannot be followed by required parameter \"" +
          param.valueName + "\"")
    }
  }
}

import scala.collection.immutable.ListMap

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
  def option[T](names: List[String], valueName: String, description: String)
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
  def multiOption[T](names: List[String],
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
  def flag[T](namesOn: List[String], namesOff: List[String], description: String)
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
  def flag[T](namesOn: List[String], description: String)
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