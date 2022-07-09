package com.estebanmarin.ziorjvm.first

import zio.*

val zioEffect: ZIO[Any, Nothing, Int] = ZIO.succeed(42)

def sequenceTakeLast[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, B] =
  zioa *> ziob
def sequenceTakeFirst[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, A] =
  zioa <* ziob
def runForever[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] =
  zio.forever
def convert[R, E, A, B](zio: ZIO[R, E, A], value: B): ZIO[R, E, B] =
  for a <- zio yield value
def asUnit[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, Unit] =
  zio.unit
def sum(n: Int): Int =
  if (n == 0) 0
  else n + sum(n - 1)

def sumZIO(n: Int): UIO[Int] =
  if (n == 0) ZIO.succeed(0)
  else
    for
      current <- ZIO.succeed(n)
      prevSum <- sumZIO(n - 1)
    yield current + prevSum
