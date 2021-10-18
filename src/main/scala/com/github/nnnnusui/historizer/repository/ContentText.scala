//package com.github.nnnnusui.historizer.repository
//
//import com.github.nnnnusui.historizer.domain.content.Text
//import com.github.nnnnusui.historizer.domain.content.Text.Self
//import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
//
//trait ContentText { self: UsesDatabase with ContentRoot =>
//  import profile.api._
//
//  val contentText = new Repository with Text.Repository {
//    case class Data(contentId: ID, value: String)
//    class TableInfo(tag: Tag) extends ContentSubTable[Data](tag, "text") {
//      def value = column[String]("value")
//      def *     = (contentId, value) <> (Data.tupled, Data.unapply)
//    }
//    val table                        = TableQuery[TableInfo]
//    override def schema: profile.DDL = table.schema
//    val joined                       = contentRoot.table join table on (_.id === _.contentId)
//
//    override def getBy(id: ID): Out[Option[Self]] = run { implicit it =>
//      for {
//        mayBe <- joined.filter(_._1.id === id).result.headOption
//      } yield for {
//        (root, data) <- mayBe
//      } yield Text(data.value)
//    }
//    override def create(self: Self): Out[ID] = run { implicit it =>
//      for {
//        id <- contentRoot.createQuery
//        _  <- table += Data(id, self.value)
//      } yield id
//    }
//    override def update(args: (ID, Self)): Out[Unit] = run { implicit it =>
//      for {
////        _ <- contentRoot.updateQuery()
//        _ <- table.update(Data(args._1, args._2.value))
//      } yield {}
//    }
//  }
//
//}
