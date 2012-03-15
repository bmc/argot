package org.clapper.argot
import java.io.File
import scala.math

object ArgotTest {
  import ArgotConverters._

  def parser = {

    val i = SingleValueOption[Int](
      List("i", "iterations"), "n", "Total iterations"
    )
/*
    flag[Int](List("v", "verbose"), List("q", "quiet"),
              "Increment (-v, --verbose) or decrement (-q, --quiet) the " +
              "verbosity level.") {
      (onOff, opt) =>

      import scala.math

      val currentValue = opt.value.getOrElse(0)
      val newValue = if (onOff) currentValue + 1 else currentValue - 1
      math.max(0, newValue)
    }

    flag[Boolean](List("n", "noerror"), "Do not abort on error.").
*/

    val u = MultiValueOption[String](
      List("u", "user"), "username",
      "User to receive email. Email address is queried from database"
    )

    val e = MultiValueOption[String](List("e", "email"), "emailaddr",
                                     "Address to receive emailed results.") {
      s =>

      val ValidAddress = """^[^@]+@[^@]+\.[a-zA-Z]+$""".r
      ValidAddress.findFirstIn(s) match {
        case None    => 
          Left("Bad email address \"" + s + "\"")
        case Some(_) => 
          Right(s)
      }
    }

    val out = SingleValueParameter[String](
      "outputfile", "Output file to which to write", false
    )

    val inputs = MultiValueParameter[File](
      "input", "Input files to read. If missing, use stdin.", true
    ) {
      s => 

      val file = new File(s)
      if (! file.exists)
        Left("Input file \"" + s + "\" does not exist.")
      else
        Right(file)
    }
    val spec = new ArgotSpecification(Seq(i, u, e), Seq(out, inputs))
    new ArgotParser(
      "test", spec,
      preUsage=Some("ArgotTest: Version 0.1. Copyright (c) " +
                    "2012, Brian M. Clapper. Pithy quotes go here.")
    )

  }

  def main(args: Array[String]) {
/*
    try {
      parser.parse(args)
      println("----------")
      println("iterations=" + iterations.value)
      println("verbose=" + verbose.value)
      println("users=" + users.value)
      println("emails=" + emails.value)
      println("output=" + output.value)
      println("input=" + input.value)
    }

    catch {
      case e: ArgotUsageException => println(e.message)
    }
*/
  }

}
