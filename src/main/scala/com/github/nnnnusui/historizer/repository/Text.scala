package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.{Text => Entity}
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait Text { self: UsesDatabase =>
  import profile.api._
  val text = new Repository {
    class TableInfo(tag: Tag) extends Table[Entity](tag, "text") {
      def *                 = (id, kind) <> (Entity.tupled, Entity.unapply)
      val id: Rep[Int]      = column[Int]("id", O.AutoInc, O.PrimaryKey)
      val kind: Rep[String] = column[String]("kind")
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

    def getAll               = run { table.result }
    def getBy(id: Entity.ID) = run { findBy(id).result.headOption }
    def create(becomeEntity: Entity.ID => Entity) =
      run { autoInc += becomeEntity(-1) }.map(becomeEntity)
    def update(entity: Entity) =
      run { findBy(entity.id).update(entity) }
        .map {
          case 0 => None
          case _ => Some(entity)
        }
  }

}
