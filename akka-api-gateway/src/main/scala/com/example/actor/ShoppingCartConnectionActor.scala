package com.example.actor

import akka.{Done, NotUsed}
import akka.actor.Actor
import akka.actor.typed.ActorRef
import akka.event.Logging
import akka.grpc.GrpcClientSettings
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.util.Timeout
import com.example.actor.SessionActor.{Command, RequestResponse}
import com.example.inventory.grpc.{InventoryService, InventoryServiceClient, ItemReply, ItemRequest}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

final case class Start()
final case class PullBuffer()
final case class SendRequest(newRequest:ItemRequest)
final case class Batch(requests:List[ItemRequest])

class ShoppingCartConnectionActor extends Actor {
  val log = Logging(context.system, this)
  implicit val askTimeout = Timeout(5.seconds)
  implicit val sys = context.system
  implicit val ec = context.system.getDispatcher

  // Take details how to connect to the service from the config.
  val clientSettings = GrpcClientSettings.fromConfig(InventoryService.name)
  // Create a client-side stub for the service
  val client: InventoryService = InventoryServiceClient(clientSettings)

  val requestStream: Source[ItemRequest, NotUsed] =
    Source.repeat(PullBuffer)
      .throttle(1, 1.second)
      .ask[Batch](self)
      .filter(x => {
        x.requests.size != 0
      })
      .mapConcat( _.requests )
      .mapMaterializedValue(_ => NotUsed)

  val responseStream: Source[ItemReply, NotUsed] = client.streamItems(requestStream)

  val done: Future[Done] =
    responseStream.runForeach(reply => {
      context.system.actorSelection(reply.sessionId).resolveOne.onComplete {
        case Success(x) =>
          x ! RequestResponse(reply)
        case Failure(e) =>
          log.error(s"Error Failed to find actor: $e")
      }
    })

  var requestBuffer = List[ItemRequest]()

  def receive = {
    case req:ItemRequest => {

      requestBuffer = requestBuffer ::: List(req)

    }
    case PullBuffer => {
      sender() ! Batch(requestBuffer)
      requestBuffer = List[ItemRequest]()
    }

    case _      => log.info("received unknown message")
  }
}