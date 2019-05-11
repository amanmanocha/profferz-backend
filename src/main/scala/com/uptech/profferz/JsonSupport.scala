package com.uptech.profferz

import com.uptech.profferz.WishActor.{AddWish, WishDetails}

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._
  implicit val userIdJsonFormat = jsonFormat1(UserId)
  implicit val wishIdJsonFormat = jsonFormat1(WishId)
  implicit val offerIdJsonFormat = jsonFormat1(OfferId)
  implicit val wishDetailsJsonFormat = jsonFormat2(WishDetails)
  implicit val wishJsonFormat = jsonFormat3(Wish)
  implicit val addWishJsonFormat = jsonFormat2(AddWish)
}
//#json-support
