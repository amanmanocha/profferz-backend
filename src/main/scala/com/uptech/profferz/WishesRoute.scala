package com.uptech.profferz

import java.util.UUID

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.model.StatusCodes.{ BadRequest, InternalServerError, NotFound }
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ as, complete, concat, entity, headerValueByName, onComplete, pathEnd, pathPrefix, put, _ }
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.pattern.ask
import akka.util.Timeout
import com.uptech.profferz.WishActor.{ AddWish, MakeOffer, OfferDetails, UpdateWish, WishDetails }

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

trait WishesRoute extends JsonSupport {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val wishesActor = system.actorOf(Props[WishesSupervisorActor])

  implicit val exceptionHandler = ExceptionHandler {
    case e: IllegalArgumentException =>
      complete(HttpResponse(BadRequest, entity = s"Invalid request ${e.getMessage}"))
    case e: NullPointerException =>
      complete(HttpResponse(NotFound, entity = s"Not found ${e.getMessage}"))
    case e: Exception =>
      complete(HttpResponse(InternalServerError, entity = s"Internal server error ${e.getMessage}"))
  }

  lazy val route: Route =
    concat(
      pathPrefix("wishes") {
        //#users-get-delete
        pathEnd {
          headerValueByName("X-User-Id") { userId =>
            post {
              entity(as[WishDetails]) { wishDetails =>
                addWish(WishId(UUID.randomUUID.toString), UserId(userId), wishDetails)
              }
            }
          }
        }
      },
      path("wishes" / JavaUUID) { id =>
        headerValueByName("X-User-Id") { userId =>
          put {
            entity(as[WishDetails]) { wishDetails: WishDetails =>
              updateWish(WishId(id.toString), UserId(userId), wishDetails)
            }
          }
        }
      },
      path("wishes" / JavaUUID / "offers") { wishId =>
        headerValueByName("X-User-Id") { userId =>
          post {
            entity(as[OfferDetails]) { offerDetails: OfferDetails =>
              makeOffer(WishId(wishId.toString), UserId(userId), offerDetails)
            }
          }
        }
      })

  def addWish(wishId: WishId, userId: UserId, wishDetails: WishDetails) = {
    val addWishCommand = AddWish(wishId, userId, wishDetails)
    onComplete(wishesActor ? addWishCommand) {
      case Success(wish: Wish) => complete(StatusCodes.Created, wish)
      case Success(offer: Offer) => complete(StatusCodes.Created, offer)
      case Success(Error(e)) => throw e
    }
  }

  def updateWish(wishId: WishId, userId: UserId, wishDetails: WishDetails) = {
    val updateWishCommand = UpdateWish(wishId, userId, wishDetails)
    onComplete(wishesActor ? updateWishCommand) {
      case Success(wish: Wish) => complete(StatusCodes.Created, wish)
      case Success(Error(e: IllegalStateException)) => addWish(wishId, userId, wishDetails)
    }
  }

  def makeOffer(wishId: WishId, userId: UserId, offerDetails: OfferDetails) = {
    val makeOfferCommand = MakeOffer(userId, wishId, offerDetails)
    onComplete(wishesActor ? makeOfferCommand) {
      case Success(wish: Wish) => complete(StatusCodes.Created, wish)
      case Success(offer: Offer) => complete(StatusCodes.Created, offer)
      case Success(Error(e)) => throw e
    }
  }

}
