package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait Text { self: UsesDatabase =>
  import profile.api._
  val text = new Repository {
    case class Data(id: Int, value: String)
    class TableInfo(tag: Tag) extends Table[Data](tag, "text") {
      val id    = column[Int]("id", O.AutoInc, O.PrimaryKey)
      val value = column[String]("value")
      def *     = (id, value) <> (Data.tupled, Data.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    private def autoInc              = table returning table.map(_.id)

    def getAll: IO[Seq[domain.Text.Identified]] = run { implicit ec =>
      table.result
    }.map(_.map { case Data(id, value) => (id.toString, domain.Text(value)) })
    def getBy(id: Int): IO[Option[domain.Text]] = run { implicit ec =>
      table.filter(_.id === id).result.headOption
    }.map(_.map { case Data(id, value) => domain.Text(value) })
    def create(from: domain.Text): IO[Int] = run { implicit ec =>
      autoInc += Data(-1, from.value)
    }
    def update(from: domain.Text.Identified): IO[Int] = run {implicit ec =>
      val (_id, domain.Text(value)) = from
      val id = _id.toInt
      table.filter(_.id === id) update Data(id, value)
    }
  }
}
