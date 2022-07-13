package com.estebanmarin.ziorjvm.first

import zio.*

object ZIOApps:
  val meaningOfLife: UIO[Int] = ZIO.succeed(42)
  @main def main: Exit[Nothing, RuntimeFlags] =
    val runtime = Runtime.default
    given trace: Trace = Trace.empty
    Unsafe.unsafeCompat { unsafe =>
      given u: Unsafe = unsafe
      runtime.unsafe.run(meaningOfLife)
    }

object betterApp extends ZIOAppDefault:
  @main override def run =
    for
      _ <- ZIOApps.meaningOfLife.debug
      _ <- Console.print(s"HERE")
    yield ()

