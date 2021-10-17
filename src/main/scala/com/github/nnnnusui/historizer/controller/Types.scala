package com.github.nnnnusui.historizer.controller

import com.github.nnnnusui.historizer.domain

object Types {
  type ID = String
  object Output {
    case class Article(id: ID, title: String)
  }
  case class Input[T](args: T)

  case class QueryArticleArgs(id: ID)
  case class MutationAddArticleArgs(title: String)

  // implicits
  import domain._
  import content._
  type Identified[T] = (Int, T)
  implicit class ArticleOutputFromDomain(self: Identified[Article]) {
    val (id, article)            = self
    def toOutput: Output.Article = Output.Article(id.toString, article.title)
  }
}
