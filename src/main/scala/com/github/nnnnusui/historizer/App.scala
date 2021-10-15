package com.github.nnnnusui.historizer

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import caliban.interop.circe.AkkaHttpCirceAdapter
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.github.nnnnusui.historizer.controller.Api
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.sslconfig.util.ConfigLoader
import zio.Runtime
import zio.clock.Clock
import zio.console.Console
import zio.internal.Platform

import scala.concurrent.ExecutionContextExecutor

object App extends App with AkkaHttpCirceAdapter {

  implicit val system: ActorSystem[String] = ActorSystem(Behaviors.empty[String], "example-app")

  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  implicit val runtime: Runtime[Service.Get with Console with Clock] =
    Runtime.unsafeFromLayer(Service.make ++ Console.live ++ Clock.live, Platform.default)

  val interpreter = runtime.unsafeRun(Api.api.interpreter)

  val publishDir = ConfigFactory.load().getString("publishDir")
  val route =
    cors() {
      path("api" / "graphql") {
        adapter.makeHttpService(interpreter)
      } ~ path("ws" / "graphql") {
        adapter.makeWebSocketService(interpreter)
      } ~ pathEndOrSingleSlash {
        getFromFile(s"$publishDir/index.html")
      } ~ getFromDirectory(publishDir)
    }

  val bindingFuture = Http().newServerAt("localhost", 8088).bind(route)

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
