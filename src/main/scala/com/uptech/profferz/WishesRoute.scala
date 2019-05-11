package com.uptech.profferz

import java.util.UUID

import akka.Done
import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, concat, entity, headerValueByName, onComplete, onSuccess, pathEnd, pathPrefix, put, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.uptech.profferz.Profferz.{system, wishesActor}
import com.uptech.profferz.WishActor.{AddWish, UpdateWish, WishDetails}
import com.uptech.profferz.WishesSupervisorActor.CreateWish

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait WishesRoute extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val wishesActor = system.actorOf(Props[WishesSupervisorActor])

  lazy val route: Route =
    concat(
      pathPrefix("wishes") {
        //#users-get-delete
        pathEnd {
          headerValueByName("X-User-Id") { userId =>
            post {
              entity(as[WishDetails]) { wishDetails =>
                addWish(UUID.randomUUID().toString, userId, wishDetails)
              }
            }
          }
        }
      },
      path("wishes" / JavaUUID) { id =>
        headerValueByName("X-User-Id") { userId =>
          put {
            entity(as[WishDetails]) { wishDetails: WishDetails =>
              val updateWishCommand = UpdateWish(wishDetails)
              val futureWishActorRef = system.actorSelection(wishesActor.path.child(s"${id}")).resolveOne()
              onComplete(futureWishActorRef) {
                case Success(actor) => {
                  val futureWish = (actor ? updateWishCommand).mapTo[Wish]
                  onComplete(futureWish) {
                    case Success(wish) => complete((StatusCodes.Created, wish))
                  }
                }
                case Failure(e) => {
                  addWish(id.toString, userId, wishDetails)
                }
              }
            }
          }
        }
      }
    )

  private def addWish(wishId:String, userId: String, wishDetails: WishDetails) = {
    val addWishCommand = AddWish(UserId(userId), wishDetails)
    val futureActorCreated = (wishesActor ? CreateWish(wishId)).mapTo[Done]
    onSuccess(futureActorCreated) { done =>
      val ref: ActorSelection = system.actorSelection(wishesActor.path.child(wishId))
      val wish: Future[Wish] = (ref ? addWishCommand).mapTo[Wish]
      onSuccess(wish) { wish =>
        complete((StatusCodes.Created, wish))
      }
    }
  }
}
