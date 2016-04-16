package com.vishr.ids.cqrs.processor

import com.vishr.ids.api.model.v1.UserDeleteCommandParams
import com.vishr.ids.api.model.v1.UserUpdateCommandParams
import com.vishr.ids.api.model.v1.UserCreateCommandParams
import com.vishr.ids.db.model.Command
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.db.model.User
import com.datastax.driver.core.utils.UUIDs
import scala.concurrent.ExecutionContext.Implicits.global

object UserCommandProcessor {
  
  def userDb = DependencyInjector.userDb
  def commandDb = DependencyInjector.commandDb
  def ancestryDb = DependencyInjector.ancestryDb
  
  
  def createUser(cmd: Command, params: UserCreateCommandParams) = {

    userDb.getUser(cmd.tenantId, params.username) flatMap { existingUser =>
      existingUser match {
        case Some(u) => throw new RuntimeException("User with name "+params.username + " already exists")
        case None => 
          val dbUser = User(cmd.tenantId,
              UUIDs.timeBased(),
              params.username,
              params.firstName,
              params.lastName)  // username changes not allowed - for now
          userDb.upsertUser(dbUser)
      }
    } map { u =>
      cmd.succeeded(Some(u.userId),Some(u.username))
    } recover { case ex:Throwable =>
        cmd.failed(
          errorCode = "BadRequest",
          errorMessage = ex.getMessage,
          None,
          Some(params.username))
    } flatMap { c =>
      commandDb.upsertCommand(c)
    }
  }

  def updateUser(cmd: Command, params: UserUpdateCommandParams) = {
    
    userDb.getUser(cmd.tenantId, params.user.userId) flatMap { existingUser =>
      existingUser match {
        case None => throw new RuntimeException("No user exists with id "+params.user.userId)
        case Some(u) => 
          val dbUser = u.copy(
              firstName = params.user.firstName,
              lastName = params.user.lastName)
          userDb.upsertUser(dbUser)
      }
    }  map { u =>
      cmd.succeeded(Some(u.userId),Some(u.username))
    } recover { case ex:Throwable =>
        cmd.failed(
          errorCode = "BadRequest",
          errorMessage = ex.getMessage,
          Some(params.user.userId),
          Some(params.user.username))
    } flatMap { c =>
      commandDb.upsertCommand(c)
    }
  }

  def deleteUser(cmd: Command, params: UserDeleteCommandParams) = {

    val removeUser = userDb.getUser(cmd.tenantId, params.userIdOrName) flatMap { user =>
      user match {
        case None => throw new RuntimeException("No user exists with key "+params.userIdOrName)
        case Some(u) => userDb.removeUser(u)
      }
    } 

    val getMemberships = removeUser flatMap { u =>
      ancestryDb.getAncestorsByMember(cmd.tenantId, u.userId, "User")
    }
    
    val removeMemberships = getMemberships flatMap { mships =>
      ancestryDb.removeEffMemberships(mships)
    } 

    removeMemberships  map { u =>
      cmd.succeeded(None,Some(params.userIdOrName))
    } recover { case ex:Throwable =>
        cmd.failed(
          errorCode = "BadRequest",
          errorMessage = ex.getMessage,
          None,
          Some(params.userIdOrName))
    } flatMap { c =>
      commandDb.upsertCommand(c)
    } 
    
  }

}