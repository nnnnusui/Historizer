package com.github.nnnnusui.historizer.domain

case class Article(title: String)
object Article {
  trait Repository extends EntityRepository[Article] {
    def getAll: Out[Seq[Identified]]
  }
}
