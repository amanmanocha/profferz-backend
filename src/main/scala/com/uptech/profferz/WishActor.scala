package com.uptech.profferz

import akka.actor._
import akka.persistence._
import com.uptech.profferz.WishActor.{AddWish, UpdateWish, WishAdded, WishUpdated}

object WishActor {
  case class WishDetails(subject:String, message:String)
  case class AddWish(userId:UserId, wishDetails:WishDetails) extends Command
  case class UpdateWish(wishDetails:WishDetails) extends Command

  case class WishAdded(wishId:WishId, userId:UserId, wishDetails: WishDetails) extends Event
  case class WishUpdated(wishDetails: WishDetails) extends Event
}

class WishActor(id: String) extends PersistentActor with ActorLogging {
  override def persistenceId = id
  var state: Wish = null

  def updateState(event: Event): Unit = {
    event match  {
      case WishAdded(id, userId, wishDetails) => {
        this.state = new Wish(id, userId, wishDetails)
      }
      case WishUpdated(wishDetails) => {
        this.state = state.copy(wishDetails = wishDetails)
      }
    }
  }

  val receiveCommand: Receive = {
    case AddWish(userId, wishDetails) => {
      persist(WishAdded(WishId(id), userId, wishDetails)) { event =>
        updateState(event)
        sender ! state
      }
    }
    case UpdateWish(wishDetails) => {
      persist(WishUpdated(wishDetails)) { event =>
        updateState(event)
        sender ! state
      }
    }
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: Wish) => state = snapshot
  }
}
