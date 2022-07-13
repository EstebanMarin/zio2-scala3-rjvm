package com.estebanmarin.ziorjvm.first

import zio.*

object ZIODependencies extends ZIOAppDefault:
  val subscriptionService: Task[UserSubscription] = ZIO.succeed(
    UserSubscription.create(
      EmailService.create(),
      UserDatabase.create(
        ConnectionPool.create(10)
      ),
    )
  )

  val subscriptionServiceLayered: ULayer[UserSubscription] = ZLayer.succeed(
    UserSubscription.create(
      EmailService.create(),
      UserDatabase.create(
        ConnectionPool.create(10)
      ),
    )
  )

  def subscribe_v2(user: User) =
    for
      //      sub: UserSubscription <- subscriptionService
      sub: UserSubscription <- ZIO.service[UserSubscription]
      _ <- sub.subscribeUser(user)
    yield ()

  val testUser = User("Esteban Marin", "esteba@marin.com")
  val testUser2 = User("Esteban Marin", "esteba@marin.com")

  val program_v2: ZIO[UserSubscription, Throwable, Unit] =
    for
      _ <- subscribe_v2(testUser)
      _ <- subscribe_v2(testUser2)
    yield ()

  //  def run = program_v2.provide(subscriptionServiceLayered)
  // ZLayers

  val connectionPoolLayer: ZLayer[Any, Nothing, ConnectionPool] =
    ZLayer.succeed(ConnectionPool.create(10))

  val dataBaseLayer: ZLayer[ConnectionPool, Nothing, UserDatabase] =
    ZLayer.fromFunction(UserDatabase.create)

  val emailServiceLayer: ZLayer[Any, Nothing, EmailService] =
    ZLayer.succeed(EmailService.create())

  val userSubscriptionServiceLayer: ZLayer[EmailService & UserDatabase, Nothing, UserSubscription] =
    ZLayer.fromFunction(UserSubscription.create)

  // combine layers
  // vertical composition
  val databaseLayerFull: ZLayer[Any, Nothing, UserDatabase] = connectionPoolLayer >>> dataBaseLayer
  // horizontal composition
  val subscriptionLayer: ZLayer[Any, Nothing, UserDatabase & EmailService] =
    databaseLayerFull ++ emailServiceLayer

  val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscription] =
    subscriptionLayer >>> userSubscriptionServiceLayer

  val runnable_v2 = program_v2.provide(
    UserSubscription.live,
    EmailService.live,
    UserDatabase.live,
    ConnectionPool.live(10)
  )

  def run = program_v2.provide(userSubscriptionLayer)


//    ZLayer.succeed(
//      UserSubscription.create(
//        EmailService.create(),
//        UserDatabase.create(
//          ConnectionPool.create(10)
//        ),
//      )
//    )
//  )
