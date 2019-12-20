package grpc

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.example.inventory.grpc.{InventoryService, ItemReply, ItemRequest}

import scala.concurrent.Future

class InventoryServiceImpl(implicit mat: Materializer) extends InventoryService {
  import mat.executionContext

  override def streamItems(in: Source[ItemRequest, NotUsed]): Source[ItemReply, NotUsed] = {
        println(s"sayHello to stream...")
        in.map(request => {
          println(s"hello, ${request.sessionId}")
          ItemReply(1000, request.sessionId)
        })
      }
}