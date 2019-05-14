package com.uptech.profferz

import com.uptech.profferz.WishActor.{ OfferDetails, WishDetails }

case class UserId(id: String)
case class WishId(id: String)
case class OfferId(id: String)

case class Wish(id: WishId, userId: UserId = null, wishDetails: WishDetails, offers: Map[UserId, Offer])
case class Offer(id: OfferId, wishId: WishId, userId: UserId, offerDetails: OfferDetails)