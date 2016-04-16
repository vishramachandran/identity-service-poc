package com.vishr.ids.cqrs.query

import scala.concurrent.Future
import com.vishr.ids.api.model.v1._
import java.util.UUID
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.common.DbToApiConversions
import scala.concurrent.ExecutionContext.Implicits.global
import com.vishr.ids.common.Constants._

trait GroupService {

  def getGroups(tenantId:UUID) : Future[List[Group]]
  def getGroup(tenantId: UUID, groupId:UUID)  : Future[Option[Group]]
  def getGroup(tenantId: UUID, groupIdOrName:String)  : Future[Option[Group]]
  def getGroupMembers(tenantId: UUID, groupIdOrName:String): Future[List[Membership]]

}

object GroupServiceImpl extends GroupService {
  
  def groupDb = DependencyInjector.groupDb
  def ancestryDb = DependencyInjector.ancestryDb
  import com.vishr.ids.common.DbToApiConversions._

  def getGroups(tenantId:UUID) : Future[List[Group]]  =
    groupDb.getGroups(tenantId) map ( _ map (convert(_)))
    
  def getGroup(tenantId: UUID, groupId:UUID)  : Future[Option[Group]] =
    groupDb.getGroup(tenantId, groupId) map ( _ map (convert(_)))

  def getGroup(tenantId: UUID, groupIdOrName:String)  : Future[Option[Group]] =
    groupDb.getGroup(tenantId, groupIdOrName) map ( _ map (convert(_)))

  def getGroupMembers(tenantId: UUID, groupIdOrName:String)  : Future[List[Membership]] = {
    if (groupIdOrName.matches(UuidRegex)) 
      ancestryDb.getDirectDescendantsByGroup(tenantId, UUID.fromString(groupIdOrName)) map ( _ map (convert(_)))
    else {
      getGroup(tenantId, groupIdOrName) flatMap { group =>
        group match {
          case Some(g) => ancestryDb.getDirectDescendantsByGroup(tenantId, g.groupId) map ( _ map (convert(_)))
          case None => Future(Nil)
        }
      }
    }

  }
  
  
}