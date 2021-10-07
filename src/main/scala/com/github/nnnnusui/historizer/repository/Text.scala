package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.{Text => Entity}
import com.github.nnnnusui.historizer.interop.slick.akka.UsesExecutionContext
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.{IO, ZIO}

trait Text { self: UsesDatabase with UsesExecutionContext =>
  import profile.api._
  val text = new Repository {
    case class Text(id: Entity.ID, value: Entity.Value)
    class TableInfo(tag: Tag) extends Table[Text](tag, "text") {
      def *                  = (id, value) <> (Text.tupled, Text.unapply)
      val id: Rep[Int]       = column[Int]("id", O.AutoInc, O.PrimaryKey)
      val value: Rep[String] = column[String]("value")
    }

    case class Own(id: Entity.ID, parent: Int)
    class OwnTable(tag: Tag) extends Table[Own](tag, "text_own") {
      def *                = (id, parent) <> (Own.tupled, Own.unapply)
      val id: Rep[Int]     = column[Int]("id", O.PrimaryKey)
      val parent: Rep[Int] = column[Int]("parent")
    }

    val table                         = TableQuery[TableInfo]
    val own                           = TableQuery[OwnTable]
    def schema                        = table.schema ++ own.schema
    private def autoInc               = table returning table.map(_.id)
    private def findBy(id: Entity.ID) = table.filter(_.id === id)

    implicit class ToTables(self: Entity) {
      def toTables: Text = Text(self.id, self.value)
    }
    implicit class ToEntity(self: Text) {
      def toEntity: Entity = Entity(self.id, self.value)
    }
    def getAll: IO[Throwable, Seq[Entity]] = run {
      for {
        it <- table.result
      } yield it.map(_.toEntity)
    }

    def getBy(id: Entity.ID): ZIO[Any, Throwable, Option[Entity]] =
      run { findBy(id).result.headOption }.map(_.map(_.toEntity))
    def create(becomeEntity: Entity.ID => Entity): IO[Throwable, Entity] = run {
      val text = becomeEntity(-1).toTables
      for {
        id <- autoInc += text
      } yield becomeEntity(id)
    }

    def updateQuery(entity: Entity) = {
      val text = entity.toTables
      for {
        updatedQuantity <- table.filter(_.id === text.id) update text
      } yield updatedQuantity match {
        case 0 => None
        case _ => Some(entity)
      }
    }
    def update(entity: Entity): IO[Throwable, Option[Entity]] = run {
      updateQuery(entity)
    }
  }
}
