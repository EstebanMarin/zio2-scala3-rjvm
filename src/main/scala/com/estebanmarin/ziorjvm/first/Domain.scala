package com.estebanmarin.ziorjvm.first

import zio.*

case class UserSubscription(emailService: EmailService, userDatabase: UserDatabase):
  def subscribeUser(user: User): Task[Unit] =
    for
      _ <- emailService.email(user)
      _ <- userDatabase.insert(user)
    yield ()

object UserSubscription:
  def create(emailService: EmailService, userDatabase: UserDatabase): UserSubscription =
    UserSubscription(emailService = emailService, userDatabase = userDatabase)

  val live: ZLayer[EmailService & UserDatabase, Nothing, UserSubscription] =
    ZLayer.fromFunction(create)

case class User(name: String, email: String)

case class EmailService():
  def email(user: User): Task[Unit] =
    ZIO.succeed(s"[EMAIL SERVICE] You've just been subscribed to Rock the JVM")

object EmailService:
  def create(): EmailService =
    EmailService()

  val live: ZLayer[Any, Nothing, EmailService] =
    ZLayer.succeed(create())

case class UserDatabase(connection: ConnectionPool):
  def insert(user: User): Task[Unit] =
    for
      connec <- connection.get
      _ <- connec.runQuery(s"saving to data base ${user.email}, ${user.name}")
    yield ()

object UserDatabase:
  def create(connect: ConnectionPool): UserDatabase =
    UserDatabase(connection = connect)

  val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
    ZLayer.fromFunction(create)

case class ConnectionPool(nConnections: Int):
  def get: Task[Connection] =
    ZIO.succeed(println("Acquired connection line")) *> ZIO.succeed(Connection())

object ConnectionPool:
  def create(nPool: Int): ConnectionPool =
    ConnectionPool(nPool)

  def live(nConnections: Int): ZLayer[Any, Nothing, ConnectionPool] =
    ZLayer.succeed(create(nConnections))

case class Connection():
  def runQuery(query: String): Task[Unit] =
    ZIO.succeed(println(s"[DATABASE] Saving to database $query"))
