package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import slick.jdbc.H2Profile
import zio.{UIO, ZIO}

trait H2Database extends UsesDatabase {
  override protected val profile = H2Profile
  import profile.api._
  override protected val database: UIO[Database] = ZIO.effectTotal(H2DBConnector.connection)
}

private object H2DBConnector {
  import slick.jdbc.JdbcBackend.Database
  val connection = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
}
