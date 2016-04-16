package com.vishr.ids.cqrs.processor

import com.vishr.ids.api.model.v1.GroupCreateCommandParams
import com.vishr.ids.api.model.v1.GroupDeleteCommandParams
import com.vishr.ids.api.model.v1.GroupUpdateCommandParams
import com.vishr.ids.db.model.Command
import com.vishr.ids.common.DependencyInjector
import com.datastax.driver.core.utils.UUIDs
import com.vishr.ids.db.model.Group
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GroupCommandProcessor {
  
  def groupDb = DependencyInjector.groupDb
  def commandDb = DependencyInjector.commandDb
  def ancestryDb = DependencyInjector.ancestryDb

  def createGroup(cmd: Command, params: GroupCreateCommandParams) = {

    groupDb.getGroup(cmd.tenantId, params.groupName) flatMap { existingGroup =>
      existingGroup match {
        case Some(u) => throw new RuntimeException("Group with name "+params.groupName + " already exists")
        case None => 
          val dbGroup = Group(cmd.tenantId,
              UUIDs.timeBased(),
              params.groupName,
              params.description)
          groupDb.upsertGroup(dbGroup)
      }
    } map { g =>
      cmd.succeeded(Some(g.groupId),Some(g.groupName))
    } recover { case ex:Throwable =>
        cmd.failed(
          errorCode = "BadRequest",
          errorMessage = ex.getMessage,
          None,
          Some(params.groupName))
    } flatMap { c =>
      commandDb.upsertCommand(c)
    }  
   
  }

  def updateGroup(cmd: Command, params: GroupUpdateCommandParams) = {
    
    groupDb.getGroup(cmd.tenantId, params.group.groupId) flatMap { existingUser =>
      existingUser match {
        case None => throw new RuntimeException("No group exists with id "+params.group.groupId)
        case Some(g) => 
          val dbGroup = g.copy(
              description = params.group.description)  // name changes not allowed - for now
          groupDb.upsertGroup(dbGroup)
      }
    } map { g =>
      cmd.succeeded(Some(g.groupId),Some(g.groupName))
    } recover { case ex:Throwable =>
        cmd.failed(
          errorCode = "BadRequest",
          errorMessage = ex.getMessage,
          Some(params.group.groupId),
          Some(params.group.groupName))
    } flatMap { c =>
      commandDb.upsertCommand(c)
    }  
    
  }

  def deleteGroup(cmd: Command, params: GroupDeleteCommandParams) = {

    val removeGroup = groupDb.getGroup(cmd.tenantId, params.groupIdOrName) flatMap { group =>
      group match {
        case None => throw new RuntimeException("No group exists with id "+params.groupIdOrName)
        case Some(g) => groupDb.removeGroup(g)
      }
    } 
    
    val getMemberships = removeGroup flatMap { g =>
      val m1 = ancestryDb.getAncestorsByMember(cmd.tenantId, g.groupId, "Group")  
      val m2 = ancestryDb.getDescendantsByGroup(cmd.tenantId, g.groupId)
      Future.sequence(List(m1,m2))
    }    

    val removeMemberships = getMemberships flatMap { mships =>
      val allMships = mships.flatten
      ancestryDb.removeEffMemberships(allMships)
    }
    
    removeMemberships  map { u =>
      cmd.succeeded(None,Some(params.groupIdOrName))
    } recover { case ex:Throwable =>
        cmd.failed(
          errorCode = "BadRequest",
          errorMessage = ex.getMessage,
          None,
          Some(params.groupIdOrName))
    } flatMap { c =>
      commandDb.upsertCommand(c)
    } 
    
  }

}