package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.Types.User
import zio.{Has, Ref, UIO, URIO, ZLayer}

object GetUserService {
  type Get = Has[Service]
  type IO[A] = URIO[Get, A]

  trait Service {
    def findUsers: UIO[List[User]]

    def findByUserId(id: Int): UIO[Option[User]]
  }

  def findUsers: IO[List[User]] = URIO.accessM(_.get.findUsers)

  def findUser(id: Int): IO[Option[User]] = URIO.accessM(_.get.findByUserId(id))

  def make(initial: List[User] = List(User(0, ""))): ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      users <- Ref.make(initial)
    } yield new Service {

      def findUsers: UIO[List[User]] = users.get

      def findByUserId(id: Int): UIO[Option[User]] = users.get.map(_.find(c => c.id == id))
    }
  }

}
