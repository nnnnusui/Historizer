package com.github.nnnnusui.historizer

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import caliban.interop.circe.AkkaHttpCirceAdapter
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import zio.Runtime
import zio.clock.Clock
import zio.console.Console
import zio.internal.Platform

import com.github.nnnnusui.historizer.controller.Api

object App extends App with AkkaHttpCirceAdapter {

  implicit val system: ActorSystem[String] = ActorSystem(Behaviors.empty[String], "example-app")

  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  implicit val runtime =
    Runtime.unsafeFromLayer(Service.make ++ Console.live ++ Clock.live, Platform.default)

  val interpreter = runtime.unsafeRun(Api.api.interpreter)

  val config     = ConfigFactory.load()
  val publishDir = config.getString("publishDir")
  val route =
    path("api" / "graphql") {
      adapter.makeHttpService(interpreter)
    } ~ path("ws" / "graphql") {
      adapter.makeWebSocketService(interpreter)
    } ~ getFromDirectory(publishDir) ~
      getFromFile(s"$publishDir/index.html")

  val bindingFuture = Http().newServerAt("127.0.0.1", 443).enableHttps(Https.context).bind(route)
  println(s"Server running at https://localhost:443")

  val redirectRoute = scheme("http") {
    extract(_.request.uri) { uri =>
      redirect(
        uri.withScheme("https"),
        StatusCodes.MovedPermanently
      )
    }
  }
  Http().newServerAt("127.0.0.1", 80).bind(redirectRoute)
  println(s"Server running at http://localhost:80")

  println("Press RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
