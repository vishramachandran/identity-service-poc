package com.vishr.ids.db

import com.vishr.ids.db.model._
import com.websudos.phantom.connectors.RootConnector
import scala.concurrent.Future
import com.datastax.driver.core.ResultSet
import com.websudos.phantom.dsl._
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.common.Constants._
import java.util.UUID

trait GroupDb {

  def upsertGroup(g:Group) : Future[Group] 
  def removeGroup(g:Group) : Future[Group]
  def getGroup(tenantId:UUID, groupIdOrName:String) : Future[Option[Group]] 
  def getGroup(tenantId:UUID, groupId:UUID) : Future[Option[Group]] 
  def getGroups(tenantId:UUID, limit:Int = 25) : Future[List[Group]] 

}

object GroupDbImpl extends GroupDb {
  
  val cassandraProvider = DependencyInjector.cassandraProvider
  import cassandraProvider._
    
  def upsertGroup(g:Group) : Future[Group] = {
    val s1 = GroupById.store(g)
    val s2 = GroupByName.store(g)
    Batch.logged.add(s1).add(s2).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => g
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
  }
    
  def removeGroup(g:Group) : Future[Group] = { 
    val s1 = GroupById.deleteGroup(g.tenantId, g.groupId)
    val s2 = GroupByName.deleteGroup(g.tenantId, g.groupName)
    Batch.logged.add(s1).add(s2).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => g
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
  }

  
  def getGroup(tenantId:UUID, groupIdOrName:String) : Future[Option[Group]] = {
    if (groupIdOrName.matches(UuidRegex)) 
      GroupById.getGroup(tenantId, UUID.fromString(groupIdOrName)).one()
    else
      GroupByName.getGroup(tenantId, groupIdOrName).one()
  }

  def getGroup(tenantId:UUID, groupId:UUID) : Future[Option[Group]] = 
    GroupById.getGroup(tenantId, groupId).one()
    
  def getGroups(tenantId:UUID, limit:Int = 25) : Future[List[Group]] =
    GroupById.getGroups(tenantId).limit(limit).fetch()
    
  
}