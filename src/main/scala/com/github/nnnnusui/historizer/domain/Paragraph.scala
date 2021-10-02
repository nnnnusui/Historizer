package com.github.nnnnusui.historizer.domain

case class Paragraph(id: Int, content: String)
object Paragraph {
  type ID      = Int
  type Content = String
  type Tupled  = (ID, Content)
  def tupled = (apply _).tupled
}
