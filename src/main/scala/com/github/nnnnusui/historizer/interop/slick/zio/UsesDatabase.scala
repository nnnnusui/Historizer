package com.github.nnnnusui.historizer.interop.slick.zio

import slick.jdbc.JdbcProfile
import zio.{IO, UIO, ZIO}

import scala.concurrent.ExecutionContext

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

//  implicit class ZioFromDbio(private val self: ZIO.type) extends AnyVal {
//    def fromDBIO[R](f: ExecutionContext => DBIO[R]): ZIO[Has[DatabaseProvider], Throwable, R] =
//      for {
//        db <- ZIO.accessM[Has[DatabaseProvider]](_.get.db)
//        r  <- ZIO.fromFuture(ec => db.run(f(ec)))
//      } yield r
//  }
  protected def run[T](dbio: ExecutionContext => DBIO[T]): IO[Throwable, T] = (
    for {
      database <- ZIO.accessM[UsesDatabase](_.database)
      result   <- ZIO.fromFuture(executionContext => database.run(dbio(executionContext)))
    } yield result
  ).provide(self)

  private object Repository {
    var instances: Seq[Repository] = Seq.empty
    def newInstance(repository: Repository): Unit =
      instances :+= repository

  }
  protected trait Repository {
    def schema: profile.SchemaDescription
    Repository.newInstance(this)
  }
}
