package com.estebanmarin.ziorjvm.first

import scala.io.StdIn.*

case class MyIO[A](unsaferun: () => A):
  def map[B](f: A => B): MyIO[B] =
    MyIO(() => f(unsaferun()))
  def flatMap[B](f: A => MyIO[B]): MyIO[B] =
    MyIO(() => f(unsaferun()).unsaferun())

case class EiIO[E, A](unSafeRun: () => Either[E, A]):
  def map[B](f: A => B): EiIO[E, B] =
    EiIO(() =>
      unSafeRun() match {
        case Left(e) => Left(e)
        case Right(v) => Right(f(v))
      }
    )

  def flatMap[B](f: A => EiIO[E, B]): EiIO[E, B] =
    EiIO(() =>
      unSafeRun() match {
        case Left(e: E) => Left(e)
        case Right(v: A) => f(v).unSafeRun()
      }
    )

case class MyZIO[-R, +E, +A](unSafeRun: R => Either[E, A]):
  def map[B](f: A => B): MyZIO[R, E, B] =
    MyZIO((r: R) =>
      unSafeRun(r) match {
        case Left(e) => Left(e)
        case Right(v) => Right(f(v))
      }
    )

  def flatMap[R1 <: R, E1 >: E, B](f: A => MyZIO[R1, E1, B]): MyZIO[R1, E1, B] =
    MyZIO((r: R1) =>
      unSafeRun(r) match {
        case Left(e: E) => Left(e)
        case Right(v: A) => f(v).unSafeRun(r)
      }
    )

def measureZIO[R, E, A](computation: MyZIO[R, E, A]): MyZIO[R, E, (Long, A)] =
  for
    a: A <- computation
    start: Long <- MyZIO(_ => Right(System.nanoTime()))
  yield (start, a)

def measureEi[E, A](computation: EiIO[E, A]): EiIO[E, (Long, A)] =
  for
    start <- EiIO(() => Right(System.nanoTime()))
    a: A <- computation
    end <- EiIO(() => Right(System.nanoTime()))
    _ <- EiIO(() => Right(println(s"type your name")))
    name <- EiIO(() => Right(readLine()))
    _ <- EiIO(() => Right(println(s"HELLO $name")))
  yield (end - start, a)

def measure[A](computation: MyIO[A]): MyIO[(Long, A)] =
  for
    start <- MyIO(() => System.nanoTime())
    a <- computation
    end <- MyIO(() => System.nanoTime())
    _ <- MyIO(() => println(s"elapsed time (nano-s): ${end - start}"))
    value <- MyIO(() => readLine())
    _ <- MyIO(() => println(s"Hello!!! ===> $value"))
  yield (end - start, a)

def printScreen = MyIO(() => println("helloWorld"))

@main def first =
//  measure(MyIO(() => print(s"Hello Esteban"))).unsaferun()
  val effect1 = measureEi(EiIO(() => Right(println("TESTING this line")))).unSafeRun()
  val effect2 = measureEi(EiIO(() => Left("ERROR"))).unSafeRun()
  val test2 = for {
    tes3: Nothing <- EiIO(() => Left("ERROR"))
  } yield ()
  for
    tes2: (Long, Unit) <- effect1
    test: (Long, Nothing) <- effect2
  yield ()
