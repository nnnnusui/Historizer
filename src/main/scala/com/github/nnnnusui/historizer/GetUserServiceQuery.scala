package com.github.nnnnusui.historizer

import Types._

object Types {
  case class QueryUserArgs(id: Int)
  case class User(id: Int, name: String)

}

object Operations {

  case class Query(
      user: QueryUserArgs => GetUserService.IO[Option[User]],
      users: GetUserService.IO[List[User]]
  )

}

