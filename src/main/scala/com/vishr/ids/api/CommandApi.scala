package com.vishr.ids.api

import akka.actor.{ ActorRefFactory, ActorSystem }
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.Constants
import com.vishr.ids.common.DependencyInjector
import java.util.UUID
import spray.routing.Directive.pimpApply

class CommandApi(implicit val system: ActorSystem,
    actorRefFactory: ActorRefFactory) extends AbstractApi  {

  import Constants._
  def commandService = DependencyInjector.commandService
  val tenantId = Constants.zero
  

  val route = {
    pathPrefix("commands") {
      get {
        // GET /commands
        pathEnd {
          respondWithMediaType(`application/json`) {
            complete(_, commandService.getRecentCommands(Constants.zero))
          }
        } ~
        // GET /commands/{id}
        path(JavaUUID) { commandId:UUID =>
          respondWithMediaType(`application/json`) {
            complete(_, commandService.getCommand(tenantId, commandId))
          }
        }
        
      }
    }
  }
}