package com.github.nnnnusui.historizer.domain

import zio.ZIO

case class RepositoryError(exception: Exception)
trait EntityRepository[Self] {
  type ID         = Int
  type Identified = (ID, Self)
  type Out[A]     = ZIO[Any, Throwable, A]
  def getBy(id: ID): Out[Option[Self]]
  def create(self: Self): Out[ID]
  def update(args: Identified): Out[Unit]
}
