package com.uptech.profferz

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.uptech.profferz.WishActor.WishDetails
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class WishesRouteSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with WishesRoute {

  "WishesRoute" should {

    "be able to add wishes (POST /wishes)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val request = Post("/wishes").withEntity(wishDetailsEntity)

      request ~> addHeader("X-User-Id", "aman") ~> route ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[Wish].wishDetails === wishDetails
        entityAs[Wish].userId === "aman"
      }
    }

    "be able to updated wishes (PUT /wishes)" in {
      val wishDetails = WishDetails("Need ICICI Bank Card", "Need ICICI bank card to buy an iPhone")
      val wishDetailsEntity = Marshal(wishDetails).to[MessageEntity].futureValue

      val postRequest = Post("/wishes").withEntity(wishDetailsEntity)
      val result = postRequest ~> addHeader("X-User-Id", "aman") ~> route ~> check {
        val postedWish = entityAs[Wish]

        val updatedWishDetails = WishDetails("Need HDFC Bank Card", "Need HDFC bank card to buy an iPhone")
        val updatedWishDetailsEntity = Marshal(updatedWishDetails).to[MessageEntity].futureValue

        val putRequest = Put(s"/wishes/${postedWish.id.id}").withEntity(updatedWishDetailsEntity)


        putRequest ~> addHeader("X-User-Id", "aman") ~> route ~> check {
          status should ===(StatusCodes.Created)

          contentType should ===(ContentTypes.`application/json`)

          entityAs[Wish].wishDetails === updatedWishDetails
          entityAs[Wish].userId === "aman"
        }
      }

    }
  }
}