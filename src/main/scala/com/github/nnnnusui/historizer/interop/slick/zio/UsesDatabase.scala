package com.github.nnnnusui.historizer.interop.slick.zio

import com.github.nnnnusui.historizer.domain.error.DomainError
import slick.jdbc.JdbcProfile
import zio.{UIO, ZIO}

trait UsesDatabase { self =>
  protected val profile: JdbcProfile
  import profile.api._
  protected val database: UIO[Database]

  private def zioFromDBIO[R](dbio: DBIO[R]): ZIO[UsesDatabase, Throwable, R] =
    for {
      db <- ZIO.accessM[UsesDatabase](_.database)
      r  <- ZIO.fromFuture(_ => db.run(dbio))
    } yield r
  protected def run[T](dbio: DBIO[T]) =
    zioFromDBIO(dbio)
      .provide(self)
      .refineOrDie { case exception: Exception =>
        DomainError.RepositoryError(exception)
      }

  protected object Repository {
    var instances: Seq[Repository] = Seq.empty
    def newInstance(repository: Repository): Unit =
      instances :+= repository
  }
  protected trait Repository {
    def schema: profile.SchemaDescription
    Repository.newInstance(this)
  }

  def setup = {
    database.map(_.run {
      DBIO.seq(Repository.instances.map(_.schema).reduceRight(_ ++ _).createIfNotExists)
    })
  }
}
