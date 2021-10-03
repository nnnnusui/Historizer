package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait Content { self: UsesDatabase =>
  import profile.api._

  import Entity._
  case class Entity(id: ID, kind: Kind)
  object Entity {
    type ID     = Int
    type Kind   = String
    type Tupled = (ID, Kind)
    def tupled = (apply _).tupled
  }
  val content = new Repository {
    class TableInfo(tag: Tag) extends Table[Entity](tag, "content") {
      def *                 = (id, kind) <> (Entity.tupled, Entity.unapply)
      val id: Rep[Int]      = column[Int]("id", O.AutoInc, O.PrimaryKey)
      val kind: Rep[String] = column[String]("kind")
    }

    case class Hierarchy(id: Entity.ID, parent: Entity.ID)
    class HierarchyTable(tag: Tag) extends Table[Hierarchy](tag, "content_closure") {
      def *                = (id, parent) <> (Hierarchy.tupled, Hierarchy.unapply)
      val id: Rep[Int]     = column[Int]("id", O.PrimaryKey)
      val parent: Rep[Int] = column[Int]("parent", O.PrimaryKey)
    }

    case class Order(parent: Entity.ID, index: Int, content: Entity.ID)
    class OrderTable(tag: Tag) extends Table[Order](tag, "content_order") {
      def *                 = (parent, index, content) <> (Order.tupled, Order.unapply)
      val parent: Rep[Int]  = column[Int]("parent", O.PrimaryKey)
      val index: Rep[Int]   = column[Int]("index", O.PrimaryKey)
      val content: Rep[Int] = column[Int]("content")
    }

    val table                         = TableQuery[TableInfo]
    val hierarchy                     = TableQuery[HierarchyTable]
    val order                         = TableQuery[OrderTable]
    def schema                        = Seq(table, hierarchy, order).map(_.schema).reduceRight(_ ++ _)
    private def autoInc               = table returning table.map(_.id)
    private def findBy(id: Entity.ID) = table.filter(_.id === id)
    def add                           = ???
    def move(id: Int, to: Int)        = ???
  }

}
