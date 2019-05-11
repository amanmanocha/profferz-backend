package com.uptech.profferz

import com.uptech.profferz.WishActor.WishDetails

case class UserId(id: String)
case class WishId(id: String)
case class OfferId(id: String)

case class Wish(id: WishId, userId: UserId = null, wishDetails: WishDetails)
case class Offer(id: OfferId, wishId: WishId, userId: UserId, message: String)