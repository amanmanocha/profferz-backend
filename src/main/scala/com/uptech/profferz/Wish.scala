package com.uptech.profferz

import com.uptech.profferz.WishActor.{OfferDetails, OfferMade, WishAdded, WishDetails, WishUpdated}

case class UserId(id: String)
case class WishId(id: String)
case class OfferId(id: String)

case class Wish(id: WishId, userId: UserId = null, wishDetails: WishDetails, offers: Map[UserId, Offer]) {
  def updateState(event: Event): Wish = {
    event match {
      case WishUpdated(wishId, wishDetails) => {
        copy(wishDetails = wishDetails)
      }
      case OfferMade(offerId, userId, wishId, offerDetails) => {
        copy(offers = offers + ((userId, Offer(offerId, wishId, userId, offerDetails))))
      }
    }
  }
}
case class Offer(id: OfferId, wishId: WishId, userId: UserId, offerDetails: OfferDetails)