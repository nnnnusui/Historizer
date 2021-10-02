package com.github.nnnnusui.historizer.interop.slick.zio

import slick.jdbc.JdbcProfile
import zio.{IO, UIO, ZIO}

object UsesDatabase {
  def setup[T <: UsesDatabase](self: T): ZIO[Any, Nothing, T] = {
    import self._
    import profile.api._
    val schema =
      Repository.instances
        .map(_.schema)
        .reduceRight(_ ++ _)
    database
      .map(_.run { DBIO.seq(schema.createIfNotExists) })
      .map(_ => self)
  }
}
trait UsesDatabase { self =>
  protected val profile: JdbcProfile
  import profile.api._
  protected val database: UIO[Database]

  protected object Repository {
    var instances: Seq[Repository] = Seq.empty
    def newInstance(repository: Repository): Unit =
      instances :+= repository
  }
  protected trait Repository {
    def schema: profile.SchemaDescription
    Repository.newInstance(this)
  }

  protected def run[T](dbio: DBIO[T]): IO[Throwable, T] =
    zioFromDBIO(dbio).provide(self)

  private def zioFromDBIO[R](dbio: DBIO[R]): ZIO[UsesDatabase, Throwable, R] =
    for {
      db <- ZIO.accessM[UsesDatabase](_.database)
      r  <- ZIO.fromFuture(_ => db.run(dbio))
    } yield r

}
