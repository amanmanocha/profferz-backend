package com.uptech.profferz

import com.uptech.profferz.WishActor.{ AddWish, OfferDetails, WishDetails }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val wishDetailsJsonFormat = jsonFormat2(WishDetails)
  implicit val offerDetailsJsonFormat = jsonFormat1(OfferDetails)
  implicit val userIdJsonFormat = jsonFormat1(UserId)
  implicit val wishIdJsonFormat = jsonFormat1(WishId)
  implicit val offerIdJsonFormat = jsonFormat1(OfferId)
  implicit val offerJsonFormat = jsonFormat4(Offer)
  implicit val wishJsonFormat = jsonFormat4(Wish)
}
