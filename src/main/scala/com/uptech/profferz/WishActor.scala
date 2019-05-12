package com.uptech.profferz

import java.util.UUID

import akka.actor._
import akka.persistence._
import com.uptech.profferz.WishActor.{AddWish, UpdateWish, WishAdded, WishUpdated}
import octopus.dsl._
import octopus.syntax._

import scala.util.Try

object WishActor {

  case class WishDetails(subject: String, message: String)

  case class AddWish(userId: UserId, wishDetails: WishDetails) extends Command

  case class UpdateWish(wishDetails: WishDetails) extends Command

  case class WishAdded(wishId: WishId, userId: UserId, wishDetails: WishDetails) extends Event

  case class WishUpdated(wishDetails: WishDetails) extends Event

  implicit val userIdValidator: Validator[UserId] = Validator[UserId]
    .rule(userId => Try(UUID.fromString(userId.id)).getOrElse(null) != null, "must be UUID")

  implicit val wishDetailsValidator: Validator[WishDetails] = Validator[WishDetails]
    .rule(_.subject.nonEmpty, "Subjecr must not be empty")
      .rule(wishDetails => wishDetails.subject.length > 10, "Subject must have length greater than 10")
      .rule(wishDetails => wishDetails.subject.length < 50, "Subject must have length less than 50")
      .rule(wishDetails => wishDetails.message.length < 100, "Message must have length less than 100")

}

class WishActor(id: String) extends PersistentActor with ActorLogging {
  override def persistenceId = id

  var state: Wish = null

  def updateState(event: Event): Unit = {
    event match {
      case addWish@WishAdded(id, userId, wishDetails) => {
        this.state = new Wish(id, userId, wishDetails)
      }
      case WishUpdated(wishDetails) => {
        this.state = state.copy(wishDetails = wishDetails)
      }
    }
  }

  val receiveCommand: Receive = {
    case AddWish if state != null => {
      sender ! Error(new IllegalStateException(s"Wish $id is already created"))
    }
    case addWish@AddWish(userId, wishDetails) if state == null => {
      val validationResult = addWish.validate
      if (validationResult.isValid) {
        persist(WishAdded(WishId(id), userId, wishDetails)) { event =>
          updateState(event)
          sender ! state
        }
      } else {
        sender ! Error(new IllegalArgumentException(validationResult.errors.map(_.message).mkString(",")))
      }

    }
    case UpdateWish if state == null => {
      sender ! Error(new IllegalStateException(s"Wish $id is not yet created"))
    }
    case updateWish@UpdateWish(wishDetails) => {
      val validationResult = updateWish.validate
      if (validationResult.isValid) {
        persist(WishUpdated(wishDetails)) { event =>
          updateState(event)
          sender ! state
        }
      } else {
        sender ! Error(new IllegalArgumentException(validationResult.errors.map(_.message).mkString(",")))
      }
    }
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: Wish) => state = snapshot
  }
}
