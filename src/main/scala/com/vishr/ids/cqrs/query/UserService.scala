package com.vishr.ids.cqrs.query

import java.util.UUID
import scala.concurrent.Future
import com.vishr.ids.common._
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.db._
import scala.concurrent.ExecutionContext.Implicits.global
import com.vishr.ids.common.Constants._

trait UserService  {
  
  def getUsers(tenantId:UUID) : Future[List[User]] 
  def getUser(tenantId: UUID, userId:UUID)  : Future[Option[User]] 
  def getUser(tenantId: UUID, userIdOrName:String)  : Future[Option[User]] 
  def getEffectiveMembership(tenantId: UUID, userIdOrName:String): Future[List[Membership]]
}

object UserServiceImpl extends UserService  {
  
  def userDb = DependencyInjector.userDb
  def ancestryDb = DependencyInjector.ancestryDb
  
  import com.vishr.ids.common.DbToApiConversions._
  
  def getUsers(tenantId:UUID) : Future[List[User]] = 
      userDb.getUsers(tenantId) map ( _ map (convert(_)))

  def getUser(tenantId: UUID, userId:UUID)  : Future[Option[User]] = 
      userDb.getUser(tenantId, userId) map ( _ map (convert(_)))

  def getUser(tenantId: UUID, userIdOrName:String)  : Future[Option[User]] = 
      userDb.getUser(tenantId, userIdOrName) map ( _ map (convert(_)))

      
  def getEffectiveMembership(tenantId: UUID, userIdOrName:String): Future[List[Membership]] = {
    val membership = 
      if (userIdOrName.matches(UuidRegex)) 
      ancestryDb.getDirectAncestorsByMember(tenantId, UUID.fromString(userIdOrName), "User") map ( _ map (convert(_)))
    else {
      getUser(tenantId, userIdOrName) flatMap { user =>
        user match {
          case Some(u) => ancestryDb.getEffectiveAncestorsByMember(tenantId, u.userId, "User") map ( _ map (convert(_)))
          case None => Future(Nil)
        }
      }
    }
    membership.map(_.distinct)
    
  }
}