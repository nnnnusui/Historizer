package com.github.nnnnusui.historizer.domain.content

import com.github.nnnnusui.historizer.domain.EntityRepository

case class Article(title: String) extends Content
object Article {
  trait Repository extends EntityRepository[Article] {
    def getAll: Out[Seq[Identified]]
  }
}
