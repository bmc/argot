/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010, Brian M. Clapper
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

/**
 * Argot is a command-line argument parsing API for Scala.
 */
package org.clapper.argot

/**
 * Base Argot exception class.
 *
 * @param message  exception message
 * @param cause    optional wrapped, or nested, exception
 */
class ArgotException(val message: String, val cause: Option[Throwable])
extends Exception(message)
{
   if (cause != None)
        initCause(cause.get)

    /**
     * Alternate constructor.
     *
     * @param message  exception message
     */
    def this(msg: String) = this(msg, None)

    /**
     * Alternate constructor.
     *
     * @param message  exception message
     * @param cause    wrapped, or nested, exception
     */
    def this(msg: String, cause: Throwable) = this(msg, Some(cause))
}

/**
 * Thrown to indicate usage errors. The calling application can catch this
 * exception and print the message, which will be a fully fleshed-out usage
 * string. For instance:
 *
 * {{{
 * import org.clapper.argot._
 *
 * ...
 *
 * val p = new Argot("MyProgram")
 * ...
 * try
 * {
 *     p.parse(args)
 * }
 *
 * catch
 * {
 *     case e: ArgotUsageException =>
 *         println(e.message)
 *         System.exit(1)
 * }
 * }}}
 *
 * @param message  exception message
 */
class ArgotUsageException(message: String)
extends ArgotException(message, None)

/**
 * Thrown to indicate that Argot could not convert a command line parameter
 * to the desired type.
 *
 * @param message  exception message
 */
class ArgotConversionException(message: String)
extends ArgotException(message, None)

/**
 * Thrown to indicate that Argot encountered a problem in the caller's
 * argument specification. This exception can be interpreted as a bug in
 * the caller's program.
 *
 * @param message  exception message
 */
class ArgotSpecificationError(message: String)
extends ArgotException("(BUG) " + message, None)
