package org.clapper.argot

object ArgotTest
{
    def main(args: Array[String])
    {
        import ArgotConverters._

        val parser = new ArgotParser(
            "test",
            preUsage=Some("ArgotTest: Version 0.1. Copyright (c) " +
                          "2010, Brian M. Clapper. Pithy quotes go here.")
        )
        val iterations = parser.option[Int](List("i", "iterations"), "n",
                                            "Total iterations")
        val sleepTime = parser.option[Int](List("s", "sleep"), "milliseconds",
                                           "Amount to sleep between each run " +
                                           "blah blah blah-de-blah yadda " +
                                           "yadda yadda ya-ya ya blah blah " +
                                           "la-de frickin da")
        val verbose = parser.flag(List("v", "verbose"), true,
                                  "Enable verbose messages")
        val user = parser.multiOption[String](List("u", "user"), "username",
                                          "Name of user to receive " +
                                          "notifications.")
        val email = parser.option[String](List("e", "email"), "emailaddr",
                                          "Addresses to email results")
        {
            (s, opt) =>
            val i = s.indexOf('@')
            if ((i < 1) || (i >= s.length))
                parser.usage("Bad email address")
            s
        }

        val output = parser.parameter[String]("output", "output file", false);
        val input = parser.parameters[Int]("input", "input count", true);

        try
        {
            parser.parse(args)
            println("----------")
            println("iterations=" + iterations.value)
            println("sleepTime=" + sleepTime.value)
            println("verbose=" + verbose.value)
            println("user=" + user.value)
            println("email=" + email.value)
            println("output=" + output.value)
            println("input=" + input.value)
        }

        catch
        {
            case e: ArgotUsageException => println(e.message)
        }
    }
}
