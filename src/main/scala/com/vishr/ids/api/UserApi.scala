package com.vishr.ids.api

import akka.actor.{ActorRefFactory, ActorSystem}
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.Constants
import com.vishr.ids.common.DependencyInjector
import spray.routing.Directive.pimpApply

class UserApi(implicit val system: ActorSystem,
                    actorRefFactory: ActorRefFactory) extends AbstractApi {

  val tenantId = Constants.zero
  def commandService = DependencyInjector.commandService
  def userService = DependencyInjector.userService

  implicit val um1 = jsonUnmarshaller[UserCreateCommandParams]
  implicit val um2 = jsonUnmarshaller[UserUpdateCommandParams]
  import Constants._
  
  val route = {
    pathPrefix("users") {
      get {
        // GET /users
        pathEnd {
          respondWithMediaType(`application/json`) {
            complete(_, userService.getUsers(tenantId))
          }
        }  ~
        // GET /users/{id}
        path(NameOrUuidRegex) { usernameOrId:String =>
          respondWithMediaType(`application/json`) {
            complete(_, userService.getUser(tenantId, usernameOrId))
          }
        } ~
        // GET /users/{id}/effectiveMembership
        path(NameOrUuidRegex / "effectiveMembership" ) { usernameOrId:String =>
          respondWithMediaType(`application/json`) {
            complete(_, userService.getEffectiveMembership(tenantId, usernameOrId))
          }
        }
      } ~
      post {
      // POST /users/create
        path("create") {
          entity(as[UserCreateCommandParams]) { params:UserCreateCommandParams =>
            respondWithMediaType(`application/json`) {
                complete(_,commandService.queueCommand(tenantId, params), Accepted)
            }
          }
        } ~              
        // TODO add new APIs to deal  with users + usernames instead of userId
        // POST /users/{id}/update
        path(NameOrUuidRegex / "update") { userIdOrName:String =>
          entity(as[UserUpdateCommandParams]) { params:UserUpdateCommandParams =>
            respondWithMediaType(`application/json`) {
                if (userIdOrName == params.user.userId.toString() || userIdOrName == params.user.username)  
                  complete(_,commandService.queueCommand(tenantId, params), Accepted)
                else 
                  completeBadRequest(_, ApiError("500","BAD_REQ", "UserId in URL and body do not match", None))
            }
          } 
        } ~
        // POST /users/{id}/delete
        path(NameOrUuidRegex / "delete") { userIdOrName:String =>
          respondWithMediaType(`application/json`) {
            complete(_, commandService.queueCommand(tenantId, UserDeleteCommandParams(userIdOrName)), Accepted)
          }
        }
      }
    }
  }
}