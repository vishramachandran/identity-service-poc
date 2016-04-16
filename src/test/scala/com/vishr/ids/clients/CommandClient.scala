package com.vishr.ids.clients

import scala.concurrent.ExecutionContext.Implicits.global
import net.liftweb.json._
import net.liftweb.json.Serialization.read
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.LiftFormats
import scala.concurrent.Future
import scala.concurrent.duration._

class CommandClient(val baseUrl: String) {

  import LiftFormats._
  import system._

  def waitCommand(c: Command, interval: FiniteDuration, count: Int): Future[Command] = {
    println("waiting for command " + c.commandId + ", " + count + " tries left")
    wsClient
      .url(baseUrl + "/commands/" + c.commandId)
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      } flatMap { c =>
        
        if (c.status == "Failure") {
          Future.failed(new RuntimeException("Command " + c + " failed"))
        } else if (c.status == "Success") {
          Future(c)
        } else if (count > 0) {
          akka.pattern.after(interval, scheduler){waitCommand(c, interval, count - 1)}
        } else {
          Future.failed(new RuntimeException("Command " + c + " timed out"))
        } 
        
      }
  }
  
  def getRecentCommands(limit:Int) = {
    wsClient
      .url(baseUrl + "/commands")
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        val commands = read[List[Command]](wsResponse.body)
        commands.take(limit)
      }

  }

}