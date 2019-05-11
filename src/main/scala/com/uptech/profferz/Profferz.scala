package com.uptech.profferz

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Profferz extends App with DefaultJsonProtocol with WishesRoute {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  Http().bindAndHandle(route, "localhost", 8080)
}
