package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait Content { self: UsesDatabase =>
  import profile.api._
  val content = new {}
}
