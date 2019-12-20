package com.example

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.actor.SessionActor._
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import com.example.actor.{SessionActor, ShoppingCartConnectionActor, User, Users}
import akka.actor.typed.scaladsl.AskPattern._
import com.example.inventory.grpc.ItemRequest
import akka.actor.typed.scaladsl.adapter._

//#import-json-formats
//#user-routes-class
class UserRoutes()(implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  val shoppingCartConActor: ActorRef = system.toClassic.actorOf(Props[ShoppingCartConnectionActor])

  def getItems(): Future[ResponsePackage] = {
    val sessionId = UUID.randomUUID().toString
    val sessionActorSystem = system.systemActorOf(SessionActor(), "session")
    sessionActorSystem.ask(SendRequest(_, 5, shoppingCartConActor, sessionId))
  }
//  def getUser(name: String): Future[GetUserResponse] =
//    userRegistry.ask(GetUser(name, _))
//  def createUser(user: User): Future[ActionPerformed] =
//    userRegistry.ask(CreateUser(user, _))
//  def deleteUser(name: String): Future[ActionPerformed] =
//    userRegistry.ask(DeleteUser(name, _))

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              rejectEmptyResponse {
                onSuccess(getItems()) { response =>
                  complete(response)
                }
              }
              //#retrieve-user-info
            }
//            ,
//            delete {
//              //#users-delete-logic
//              onSuccess(deleteUser(name)) { performed =>
//                complete((StatusCodes.OK, performed))
//              }
//              //#users-delete-logic
//            }
      )
        })
      //#users-get-delete
    }
  //#all-routes
}
