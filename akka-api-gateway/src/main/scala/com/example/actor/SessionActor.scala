package com.example.actor

//#user-registry-actor
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.example.inventory.grpc.{ItemReply, ItemRequest}

import scala.collection.immutable

//#user-case-classes
final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object SessionActor {
  // actor protocol
  sealed trait Command
  final case class SendRequest(replyTo: ActorRef[ResponsePackage], requestsNum:Int, connectionActor: akka.actor.ActorRef, sessionId:String) extends Command
  final case class RequestResponse(itemReply: ItemReply) extends Command

  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)
  case class ResponsePackage(responses: Int)


  def apply(): Behavior[Command] = session(responses = List[ItemReply](), 0, null)

  def session(responses: List[ItemReply], numberOfRequests:Int, replyTo:ActorRef[ResponsePackage]): Behavior[Command] =
    Behaviors.receiveMessage {
      case SendRequest(r, requestsNum, connectionActor, sessionId) =>

        (0 to requestsNum).toList
          .map ( _ => connectionActor ! ItemRequest("123", sessionId))

        SessionActor.session(responses, numberOfRequests, r)
      case RequestResponse(reply) =>

        val newResponses = responses ::: List[ItemReply](reply)

        if(newResponses.size >= numberOfRequests){
          replyTo ! ResponsePackage(newResponses.size)
        }

        SessionActor.session(newResponses, numberOfRequests, replyTo)
    }
}
//#user-registry-actor
