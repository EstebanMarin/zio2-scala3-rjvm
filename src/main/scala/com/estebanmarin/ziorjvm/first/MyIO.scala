package com.estebanmarin.ziorjvm.first
import scala.io.StdIn.*

case class MyIO[A](unsaferun: () => A):
  def map[B](f: A => B): MyIO[B] =
    MyIO(() => f(unsaferun()))
  def flatMap[B](f: A => MyIO[B]): MyIO[B] =
    MyIO(() => f(unsaferun()).unsaferun())

def measure[A](computation: MyIO[A]): MyIO[(Long, A)] =
  for
    start <- MyIO(() => System.nanoTime())
    a <- computation
    end <- MyIO(() => System.nanoTime())
    _  <- MyIO(() => println(s"elapsed time (nano-s): ${end-start}"))
    value <- MyIO( ()  => readLine())
    _  <- MyIO(() => println(s"Hello!!! ===> $value"))
  yield (end - start, a)

def printScreen = MyIO(() => println("helloWorld"))

@main def first =
  measure(MyIO(() => print(s"Hello Esteban"))).unsaferun()
