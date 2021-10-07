package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.{History => Entity}
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.IO

trait History { self: UsesDatabase =>
  import profile.api._

  val history = new Repository {
    val tableName = "history"

    class TableInfo(tag: Tag) extends Table[Entity.Base](tag, tableName) {
      def *            = (id) <> (Entity.Base.apply, Entity.Base.unapply)
      val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    }

    //    case class AddText(historyId: Entity.ID, textId: Text.ID, offset: Int, text: String)
    //    implicit class RichAddText(self: Entity.AddText) {
    //      def toTable = AddText(self.base.id, self.textId, self.offset, self.text)
    //    }
    //    class AddTextTable(tag: Tag) extends Table[AddText](tag, s"${tableName}_add") {
    //      def *                   = (historyId, textId, offset, text) <> (AddText.tupled, AddText.unapply)
    //      val historyId: Rep[Int] = column[Int]("history_id", O.PrimaryKey)
    //      val textId: Rep[Int]    = column[Int]("text_id")
    //      val offset: Rep[Int]    = column[Int]("offset")
    //      val text: Rep[String]   = column[String]("text")
    //    }
    //    class RemoveTextTable(tag: Tag) extends Table[Entity.RemoveText](tag, s"${tableName}_add") {
    //      def * =
    //        (historyId, textId, offset, text) <> (Entity.RemoveText.tupled, Entity.RemoveText.unapply)
    //      val historyId: Rep[Int] = column[Int]("history_id", O.PrimaryKey)
    //      val textId: Rep[Int]    = column[Int]("text_id")
    //      val offset: Rep[Int]    = column[Int]("offset")
    //      val text: Rep[String]   = column[String]("text")
    //    }

    val table = TableQuery[TableInfo]

    //    val addText    = TableQuery[AddTextTable]
    //    val removeText = TableQuery[RemoveTextTable]
    //    val joined = for {
    //      root   <- table
    //      add    <- addText if root.id === add.historyId
    //      remove <- removeText if root.id === remove.historyId
    //    } yield (root, add, remove)
    //    def schema          = Seq(table, addText, removeText).map(_.schema).reduceRight(_ ++ _)
    def schema = table.schema

    private def autoInc = table returning table.map(_.id)
    //    private def findBy(id: Entity.ID) = {
    //      (table.filter(_.id === id) joinLeft
    //        addText on (_.id === _.historyId) joinLeft
    //        removeText on (_._1.id === _.historyId))
    //        .map { case ((a, b), c) => (a, b, c) }
    //    }

    def getAll: IO[Throwable, Seq[Entity.Base]] = run { table.result }

    //    def getBy(id: Entity.ID): ZIO[Any, Throwable, Option[Product]] = run {
    //      findBy(id).result.headOption
    //    }.map(_.fold[Option[History]](None)(it => it._2.getOrElse(it._3)))
    def create(becomeEntity: Entity.ID => Entity.Base): IO[Throwable, Entity.Base] =
      run { autoInc += becomeEntity(-1) }
        .map(becomeEntity)
    //      becomeEntity(-1) match {
    //        case it: History.AddText => it.toTable
    //        case History.RemoveText(base, textId, offset, text) => ???
    //      }
    //      run { (autoInc += becomeEntity(-1).base) }
    //        .map(becomeEntity)
    //        .map {
    //          case it: Entity.AddText    => (it, addText += it)
    //          case it: Entity.RemoveText => (it, removeText += it)
    //        }
    //        .map { case (it, query) => run(query).map(_ => it) }
    //        .flatten
    //    }
    //
    //    def update(entity: Entity) =
    //      run { findBy(entity.id).update(entity) }
    //        .map {
    //          case 0 => None
    //          case _ => Some(entity)
    //        }
  }
}
