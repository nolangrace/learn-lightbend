package com.example.actor

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorRef}
import akka.stream.scaladsl.{Sink, Source}
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.AskPattern._
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.actor.SessionActor.ResponsePackage

import scala.concurrent.duration._


case class simulatorStart(shoppingCartConActor: ActorRef)

class Simulator extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {

    case simulatorStart(shoppingCartConActor) => {

      log.info("Start")

      implicit val timeout = Timeout.create(context.system.settings.config.getDuration("my-app.routes.ask-timeout"))
      implicit val sched = context.system.toTyped.scheduler
      implicit val sys = context.system

      Source.repeat(1)
        .throttle(1000, 1.second)
        .map(x=> {
//        log.info("Creating Session")

        val sessionId = UUID.randomUUID().toString
        val sessionActorSystem = context.spawn(SessionActor(), "session-"+sessionId)

        val num = 1 + scala.util.Random.nextInt( 10 )

        sessionActorSystem ! SessionActor.SendRequest(self, num, shoppingCartConActor, sessionId)
      }).to(Sink.ignore).run()

    }
    case r:ResponsePackage => log.info("Simulated Request completed, Size: "+r.responses)
    case _      => log.info("received unknown message")
  }
}
