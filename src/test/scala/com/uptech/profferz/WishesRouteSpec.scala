package com.uptech.profferz

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.uptech.profferz.WishActor.{OfferDetails, WishDetails}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class WishesRouteSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with WishesRoute {
  implicit val testTimeout = {
    RouteTestTimeout(5.seconds dilated)
  }

  "WishesRoute" should {

    "be able to add wishes (POST /wishes)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val request = Post("/wishes").withEntity(wishDetailsEntity)

      request ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[Wish].wishDetails === wishDetails
        entityAs[Wish].userId === "bob"
      }
    }

    "not allow empty subject (POST /wishes)" in {
      val wishDetails = WishDetails("", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val request = Post("/wishes").withEntity(wishDetailsEntity)

      request ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "not allow large subject (POST /wishes)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone." +
        "Need ICICI bank card to buy an iPhone." +
        "Need ICICI bank card to buy an iPhone." +
        "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val request = Post("/wishes").withEntity(wishDetailsEntity)

      request ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "not allow big message (POST /wishes)" in {
      val wishDetails = WishDetails("small", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val request = Post("/wishes").withEntity(wishDetailsEntity)

      request ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "be able to updated wishes (PUT /wishes)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val postRequest = Post("/wishes").withEntity(wishDetailsEntity)
      postRequest ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        val postedWish = entityAs[Wish]

        val updatedWishDetails = WishDetails("Need HDFC Bank Card", "Need HDFC bank card to buy an iPhone")
        val updatedWishDetailsEntity = Marshal(updatedWishDetails).to[MessageEntity].futureValue

        val putRequest = Put(s"/wishes/${postedWish.id.id}").withEntity(updatedWishDetailsEntity)

        putRequest ~> addHeader("X-User-Id", "bob") ~> route ~> check {
          status should ===(StatusCodes.Created)

          contentType should ===(ContentTypes.`application/json`)

          entityAs[Wish].wishDetails === updatedWishDetails
          entityAs[Wish].userId === "bob"
        }
      }
    }

    "allow users to make offers to wishes (POST /wishes/{id}/offers)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val postRequest = Post("/wishes").withEntity(wishDetailsEntity)
      postRequest ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        val postedWish = entityAs[Wish]

        val offer = OfferDetails("You can use mine. I will charge 10 percent.")
        val offerDetailsEntity = Marshal(offer).to[MessageEntity].futureValue

        val putRequest = Post(s"/wishes/${postedWish.id.id}/offers").withEntity(offerDetailsEntity)

        putRequest ~> addHeader("X-User-Id", "joe") ~> route ~> check {
          status should ===(StatusCodes.Created)

          contentType should ===(ContentTypes.`application/json`)

          entityAs[Offer].offerDetails === offer
          entityAs[Offer].userId === "joe"
        }
      }
    }

    "should not allow users to make multiple offers to wishes (POST /wishes/{id}/offers)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val postRequest = Post("/wishes").withEntity(wishDetailsEntity)
      postRequest ~> addHeader("X-User-Id", "bob") ~> route ~> check {
        val postedWish = entityAs[Wish]

        val offer = OfferDetails("You can use mine. I will charge 10 percent.")
        val offerDetailsEntity = Marshal(offer).to[MessageEntity].futureValue

        val putRequest = Post(s"/wishes/${postedWish.id.id}/offers").withEntity(offerDetailsEntity)

        putRequest ~> addHeader("X-User-Id", "joe") ~> route ~> check {
          val postedOffer = entityAs[Offer]

          val anotherOffer = OfferDetails("Another offer. I will charge 20 percent.")
          val anotherOfferEntity = Marshal(anotherOffer).to[MessageEntity].futureValue

          val putRequestFromSameUserToSameWish = Post(s"/wishes/${postedWish.id.id}/offers").withEntity(anotherOfferEntity)

          putRequest ~> addHeader("X-User-Id", "joe") ~> route ~> check {
            status should ===(StatusCodes.InternalServerError)
          }
        }
      }
    }

  }
}