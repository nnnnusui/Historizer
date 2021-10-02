package com.github.nnnnusui.historizer.domain.error

sealed trait DomainError extends Throwable
object DomainError {
  case class RepositoryError(cause: Exception) extends DomainError
}
