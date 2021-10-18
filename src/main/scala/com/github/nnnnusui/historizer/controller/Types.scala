package com.github.nnnnusui.historizer.controller

import com.github.nnnnusui.historizer.domain

object Types {
  type ID = String
  case class Input[T](args: T)
  object Output {
    case class Article(id: ID, title: String)
    case class Content(id: ID)
  }

  case class QueryArticleArgs(id: ID)
  case class MutationAddArticleArgs(title: String)

  case class QueryContentArgs(id: ID)
  sealed trait ContentUnion
  object ContentUnion {
    case object Paragraph extends ContentUnion
//    case object Section   extends ContentUnion
  }
  type MutationAddContentArgs = ContentUnion
  case class SubscriptionAddedContent(parentId: ID)

  // implicits
  import domain._
  import content._
  type Identified[T] = (Int, T)
  implicit class ArticleDomainFromInput(self: MutationAddArticleArgs) {
    def toDomain: Article = Article(self.title)
  }
  implicit class ArticleOutputFromDomain(self: Identified[Article]) {
    val (id, article)            = self
    def toOutput: Output.Article = Output.Article(id.toString, article.title)
  }

  implicit class ContentDomainFromInput(self: MutationAddContentArgs) {
    def toDomain: Content = self match {
      case ContentUnion.Paragraph => Content.Paragraph
    }
  }
  implicit class ContentOutputFromDomain(self: Identified[Content]) {
    val (id, content)            = self
    def toOutput: Output.Content = Output.Content(id.toString)
  }
}
