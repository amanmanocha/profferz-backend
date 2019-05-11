package com.uptech.profferz

import akka.Done
import akka.actor.{Actor, Props}
import com.uptech.profferz.WishesSupervisorActor.CreateWish

object WishesSupervisorActor {
  case class CreateWish(id:String)
}
class WishesSupervisorActor extends Actor {
  override def receive: Receive = {
    case command: CreateWish => {
      context.actorOf(Props(new WishActor(command.id)), command.id)
      sender() ! Done
    }
  }
}
