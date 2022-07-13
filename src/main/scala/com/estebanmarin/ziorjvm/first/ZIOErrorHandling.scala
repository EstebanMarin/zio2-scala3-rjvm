package com.estebanmarin.ziorjvm.first

import zio.*

import scala.util.{ Failure, Success, Try }

object ZIOErrorHandling:
  val zio: Task[RuntimeException] = ZIO.fail(new RuntimeException("BOOOMMMM!"))
  val zio2: ZIO[Any, String | Null, Nothing] =
    ZIO.fail(new RuntimeException("BOOOMMMM!")).mapError(_.getMessage)

  // effectfully catch errors
  val getAll = zio.catchAll(e => ZIO.succeed(s"Returning a different one"))
  val catchSelectiveErrors = zio.catchSome {
    case e: RuntimeException => ZIO.succeed(e)
  }
  val abetterZIo = ZIO.attempt {
    print("trying something")
    val string: String = ""
    string.length
  }

  val abetterAttemt = abetterZIo.orElse(ZIO.succeed(42))
  val otherAttempt = abetterZIo.fold(e => s"${e.getMessage()}", e => s"${e}")
  val antZIOAttempt = abetterZIo.foldZIO(
    e => ZIO.succeed(42),
    e => ZIO.succeed(42),
  )

  def fromTry[A](z: Try[A]): Task[A] =
    z match
      case Success(a) => ZIO.succeed(a)
      case Failure(error) => ZIO.fail(error)

  // Cause Error, and Die un ckecked error

  val fail = ZIO.fail("I fail")
  val exposedCause: ZIO[Any, Cause[String], Nothing] = fail.sandbox
  val unexposed: ZIO[Any, String, Nothing] = exposedCause.unsandbox

  // fold with cause
  val foldedWithCause: URIO[Any, String] =  fail.foldCause(cause => s"this is the ${cause.defects}", value => "this succedd")

  //exercices
  // type the error
  val aBadFail: ZIO[Any, Nothing, Int] = ZIO.succeed[Int](throw new RuntimeException("error"))
  val aBetterfail: ZIO[Any, Throwable, Int] = aBadFail.unrefine{
    case e => e
  }


