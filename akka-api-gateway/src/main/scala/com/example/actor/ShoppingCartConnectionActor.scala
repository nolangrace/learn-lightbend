package com.example.actor

import akka.NotUsed
import akka.actor.Actor
import akka.event.Logging
import akka.grpc.GrpcClientSettings
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.util.Timeout
import com.example.inventory.grpc.{InventoryService, InventoryServiceClient, ItemRequest}

import scala.concurrent.duration._

final case class Start()
final case class GetBatch()
final case class SendRequest(newRequest:ItemRequest)
final case class Batch(requests:List[Request])

case class Request()

class ShoppingCartConnectionActor extends Actor {
  val log = Logging(context.system, this)
  implicit val askTimeout = Timeout(5.seconds)
  implicit val sys = context.system
  implicit val ec = context.system.getDispatcher

  // Take details how to connect to the service from the config.
  val clientSettings = GrpcClientSettings.fromConfig(InventoryService.name)
  // Create a client-side stub for the service
  val client: InventoryService = InventoryServiceClient(clientSettings)

  val actorSource:Source[Int, SourceQueueWithComplete[Int]] = Source
    .queue[Int](50, OverflowStrategy.backpressure)

  val requestStream: Source[ItemRequest, NotUsed] =
    actorSource
      .map(i => {
        println("Sending request: " + i)
        ItemRequest(s"Alice-$i")
      })
      .mapMaterializedValue(_ => NotUsed)

  client.streamItems(requestStream)
      .map(x => {
        context.system.actorSelection("user/session-" +x.sessionId) ! x
      })

  val sendRequestQueue:SourceQueueWithComplete[ItemRequest] = Source
    .queue[ItemRequest](10, OverflowStrategy.backpressure)
    .toMat(Sink.ignore)(Keep.left)
    .run()



  def receive = {
    case SendRequest(newRequest) => {
      sendRequestQueue.offer(newRequest).map {
        case QueueOfferResult.Enqueued    => println(s"enqueued $newRequest")
        case QueueOfferResult.Dropped     => println(s"dropped $newRequest")
        case QueueOfferResult.Failure(ex) => println(s"Offer failed ${ex.getMessage}")
        case QueueOfferResult.QueueClosed => println("Source Queue closed")
      }
    }
    case _      => log.info("received unknown message")
  }
}