package com.example

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.actor.SessionActor._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.util.Timeout
import com.example.actor.{SessionActor, ShoppingCartConnectionActor, Simulator, User, Users, start}
import akka.actor.typed.scaladsl.AskPattern._
import com.example.inventory.grpc.ItemRequest
import akka.actor.typed.scaladsl.adapter._

import scala.concurrent.duration._

//#import-json-formats
//#user-routes-class
class UserRoutes(context:ActorContext[Nothing])(implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

//  implicit val timeout: Timeout = 3.seconds

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  val shoppingCartConActor: ActorRef = context.actorOf(Props[ShoppingCartConnectionActor], "shopping-cart-connection")

  val simulationActor: ActorRef = context.actorOf(Props[Simulator], "Simulator")
  simulationActor ! start(shoppingCartConActor)

  def getItems(): Future[ResponsePackage] = {
    val sessionId = UUID.randomUUID().toString
    val sessionActorSystem = context.spawn(SessionActor(), "session-"+sessionId)

    val num = 1 + scala.util.Random.nextInt( 10 )

    sessionActorSystem.ask(replyTo => SendRequest(replyTo, num, shoppingCartConActor, sessionId))
  }

  val userRoutes =
    path("hello") {
      get {
        onSuccess(getItems()) { response =>
                            complete(response)
                          }
      }
    }
}
