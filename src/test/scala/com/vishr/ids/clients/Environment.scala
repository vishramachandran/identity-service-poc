package com.vishr.ids.clients

import akka.actor.ActorSystem
import play.api.libs.ws.ning.NingWSClient

object system {
  val system = ActorSystem()
  val scheduler = system.scheduler
  val baseUrl = "http://localhost:8000"
  val ec = system.dispatcher
  val wsClient = NingWSClient()
  val userClient = new UserClient(baseUrl)
  val groupClient = new GroupClient(baseUrl)
  val commandClient = new CommandClient(baseUrl)
}

