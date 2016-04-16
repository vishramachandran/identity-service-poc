package com.vishr.ids.api

import akka.actor.{ActorRefFactory, ActorSystem}
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.Constants
import com.vishr.ids.common.DependencyInjector
import spray.routing.Directive.pimpApply

class GroupApi(implicit val system: ActorSystem,
                    actorRefFactory: ActorRefFactory) extends AbstractApi {

  def commandService = DependencyInjector.commandService
  def groupService = DependencyInjector.groupService

    
  implicit val um1 = jsonUnmarshaller[GroupCreateCommandParams]
  implicit val um2 = jsonUnmarshaller[GroupUpdateCommandParams]
  implicit val um3 = jsonUnmarshaller[MembershipCreateParams]
  implicit val um4 = jsonUnmarshaller[MembershipDeleteParams]
  
  val tenantId = Constants.zero

  import Constants._
  val route = {
    pathPrefix("groups") {
      get {
        // GET /groups
        pathEnd {
          respondWithMediaType(`application/json`) {
            complete(_,groupService.getGroups(tenantId))
          }
        }  ~
        // GET /groups/{id}
        path(NameOrUuidRegex) { groupNameOrId:String =>
          respondWithMediaType(`application/json`) {
            complete(_,groupService.getGroup(tenantId, groupNameOrId))
          }
        } ~
        // GET /groups/{id}/members
        path(NameOrUuidRegex / "members" ) { groupIdOrName:String =>
          respondWithMediaType(`application/json`) {
            complete(_,groupService.getGroupMembers(tenantId, groupIdOrName))
          }
        }
      } ~
      post {
        // POST /groups/create
        path("create") {
          entity(as[GroupCreateCommandParams]) { params:GroupCreateCommandParams =>
            respondWithMediaType(`application/json`) {
                complete(_,commandService.queueCommand(tenantId, params), Accepted)
            }
          }
        } ~
        // POST /groups/{id}/update
        path(NameOrUuidRegex / "update" ) { groupIdOrName:String =>
          entity(as[GroupUpdateCommandParams]) { params:GroupUpdateCommandParams =>
            respondWithMediaType(`application/json`) {
                if (groupIdOrName == params.group.groupId.toString() || groupIdOrName == params.group.groupName)  
                  complete(_,commandService.queueCommand(tenantId, params), Accepted)
                else 
                  completeBadRequest(_, ApiError("500","BAD_REQ", "GroupId in URL and body do not match", None))
            }
          }
        } ~
        // POST /groups/{id}/delete
        path(NameOrUuidRegex / "delete") { groupIdOrName:String =>
          respondWithMediaType(`application/json`) {
            complete(_, commandService.queueCommand(tenantId, GroupDeleteCommandParams(groupIdOrName)), Accepted)
          }
        } ~
        // POST /groups/{id}/addMember
        path(NameOrUuidRegex / "addMember" ) { groupIdOrName:String =>
          entity(as[MembershipCreateParams]) { params:MembershipCreateParams =>
            respondWithMediaType(`application/json`) {
                if (groupIdOrName == params.groupIdOrName)  
                  complete(_,commandService.queueCommand(tenantId, params), Accepted)
                else 
                  completeBadRequest(_, ApiError("500","BAD_REQ", "GroupId in URL and body do not match", None))
            }
          }
        } ~
        // POST /groups/{id}/removeMember
        path(NameOrUuidRegex / "removeMember" ) { groupIdOrName:String =>
          entity(as[MembershipDeleteParams]) { params:MembershipDeleteParams =>
            respondWithMediaType(`application/json`) {
                if (groupIdOrName == params.groupIdOrName)  
                  complete(_,commandService.queueCommand(tenantId, params), Accepted)
                else 
                  completeBadRequest(_, ApiError("500","BAD_REQ", "GroupId in URL and body do not match", None))
            }
          }
        } 
        
      }
    }
   }
}