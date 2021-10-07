package com.github.nnnnusui.historizer.domain

case class ActionResult[T](state: T, undoAction: Action[T])
