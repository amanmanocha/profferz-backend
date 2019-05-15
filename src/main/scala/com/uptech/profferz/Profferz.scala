package com.uptech.profferz

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.persistence.eventstore.query.scaladsl.EventStoreReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import com.uptech.profferz.WishActor.WishAdded
import com.uptech.profferz.read.WishesView
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Profferz extends App with DefaultJsonProtocol with WishesRoute {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  WishesView.startPopulatingView

  Http().bindAndHandle(route, "localhost", 8080)
}