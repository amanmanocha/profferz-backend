package com.uptech.profferz

import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

class WishesSupervisorActor extends Actor {

  override def receive: Receive = {
    case command: Command => {
      implicit val timeout = 1.seconds
      Try(context.actorOf(Props(new WishActor(command.wishId.id)), command.wishId.id) forward command)
        .getOrElse(context.actorSelection(self.path.child(command.wishId.id)) forward command)
    }
  }
}
