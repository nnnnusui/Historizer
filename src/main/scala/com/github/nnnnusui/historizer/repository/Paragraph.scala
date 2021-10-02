package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.{Paragraph => Entity}
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait Paragraph { self: UsesDatabase =>
  import profile.api._

  val paragraph = new Repository {
    val tableName = "paragraph"
    class TableInfo(tag: Tag) extends Table[Entity](tag, tableName) {
      def *                    = (id, content) <> (Entity.tupled, Entity.unapply)
      val id: Rep[Int]         = column[Int]("id", O.AutoInc, O.PrimaryKey)
      val content: Rep[String] = column[String]("content")
    }

    val table                         = TableQuery[TableInfo]
    def schema                        = table.schema
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
