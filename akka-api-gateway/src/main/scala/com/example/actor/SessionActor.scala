package com.example.actor

//#user-registry-actor
import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.Sink
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
    Behaviors.setup { context =>

      Behaviors.receiveMessage {
        case SendRequest(r, requestsNum, connectionActor, sessionId) =>

          println("session ID: " + sessionId + " Actor id: "+context.self.path.toStringWithoutAddress+" ReplyTO:"+r.path)

          (1 to requestsNum).toList
            .map(_ => connectionActor ! ItemRequest("123", context.self.path.toStringWithoutAddress))


          SessionActor.session(responses, requestsNum, r)
        case RequestResponse(reply) =>
          val newResponses = responses ::: List(reply)

          println("Received Request: "+newResponses.size)

          if (newResponses.size == numberOfRequests) {
            replyTo ! ResponsePackage(newResponses.size)

            println("Session Completed: Responses: "+newResponses.size+" total: "+numberOfRequests)

            Behaviors.stopped
          }

          SessionActor.session(newResponses, numberOfRequests, replyTo)
      }
    }
}
//#user-registry-actor
