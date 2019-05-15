package com.uptech.profferz

import java.util.UUID

import akka.actor._
import akka.persistence._
import com.uptech.profferz.WishActor.{ AddWish, MakeOffer, OfferMade, UpdateWish, WishAdded, WishUpdated }
import octopus.dsl._
import octopus.syntax._
import akka.pattern.ask

import scala.util.Try

object WishActor {

  case class WishDetails(subject: String, message: String)

  case class OfferDetails(message: String)

  case class AddWish(override val wishId: WishId, userId: UserId, wishDetails: WishDetails) extends Command

  case class UpdateWish(override val wishId: WishId, userId: UserId, wishDetails: WishDetails) extends Command

  case class MakeOffer(userId: UserId, wishId: WishId, offerDetails: OfferDetails) extends Command

  case class WishAdded(wishId: WishId, userId: UserId, wishDetails: WishDetails) extends Event

  case class WishUpdated(wishId: WishId, wishDetails: WishDetails) extends Event

  case class OfferMade(offerId: OfferId, userId: UserId, wishId: WishId, offerDetails: OfferDetails) extends Event

  implicit val userIdValidator: Validator[UserId] = Validator[UserId]
    .rule(userId => Try(UUID.fromString(userId.id)).getOrElse(null) != null, "must be UUID")

  implicit val wishDetailsValidator: Validator[WishDetails] = Validator[WishDetails]
    .rule(_.subject.nonEmpty, "Subjecr must not be empty")
    .rule(wishDetails => wishDetails.subject.length > 10, "Subject must have length greater than 10")
    .rule(wishDetails => wishDetails.subject.length < 50, "Subject must have length less than 50")
    .rule(wishDetails => wishDetails.message.length < 100, "Message must have length less than 100")

  implicit val offerDetailsValidator: Validator[OfferDetails] = Validator[OfferDetails]
    .rule(offerDetails => offerDetails.message.length < 100, "Message must have length less than 100")

}

class WishActor(id: String) extends PersistentActor with ActorLogging {
  override def persistenceId = id

  var state: Wish = null

  def updateState(event: Event): Unit = {
    event match {
      case addWish @ WishAdded(id, userId, wishDetails) => {
        this.state = new Wish(id, userId, wishDetails, Map.empty)
      }
      case _ => state = state.updateState(event)
    }
  }

  val receiveCommand: Receive = {
    case addWish @ AddWish if state != null => {
      sender ! Error(new IllegalStateException(s"Wish $id is already created"))
    }
    case addWish @ AddWish(_, userId, wishDetails) if state == null => {
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
    case command:Command if state == null => {
      sender ! Error(new IllegalStateException(s"Wish with id $id does not exist"))
    }
    case updateWish @ UpdateWish(wishId, _, wishDetails) => {
      val validationResult = updateWish.validate
      if (validationResult.isValid) {
        persist(WishUpdated(wishId, wishDetails)) { event =>
          updateState(event)
          sender ! state
        }
      } else {
        sender ! Error(new IllegalArgumentException(validationResult.errors.map(_.message).mkString(",")))
      }
    }

    case makeOffer @ MakeOffer(userId, wishId, offerDetails) => {
      val validationResult = makeOffer.validate
      if (state.offers.contains(userId)) {
        sender ! Error(new IllegalStateException(s"$userId has already made offer to $userId"))
      } else {
        if (validationResult.isValid) {
          val offerId = OfferId(UUID.randomUUID.toString)
          persist(OfferMade(offerId, userId, wishId, offerDetails)) { event =>
            updateState(event)
            sender ! state.offers(userId)
          }
        } else {
          sender ! Error(new IllegalArgumentException(validationResult.errors.map(_.message).mkString(",")))
        }
      }
    }
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: Wish) => state = snapshot
  }
}
