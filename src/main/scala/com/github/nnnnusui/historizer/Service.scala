package com.github.nnnnusui.historizer

import zio.{Has, Ref, ZIO, ZLayer}

object Service {
  trait Service {}
  type Get   = Has[Service]
  type IO[A] = ZIO[Get, Throwable, A]

  def make: ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      _ <- Ref.make("")
    } yield new Service {}
  }
}
