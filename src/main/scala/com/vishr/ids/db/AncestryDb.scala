package com.vishr.ids.db

import com.vishr.ids.db.model._
import com.websudos.phantom.connectors.RootConnector
import scala.concurrent.Future
import com.datastax.driver.core.ResultSet
import com.websudos.phantom.dsl._
import com.vishr.ids.common.DependencyInjector

trait AncestryDb {
  
  def upsertEffMemberships(mships: List[EffMembership]): Future[List[EffMembership]]
  def removeEffMemberships(mships: List[EffMembership]): Future[List[EffMembership]]
  def getDescendantsByGroup(tenantId: UUID, groupId: UUID): Future[List[EffMembership]] 
  def getAncestorsByMember(tenantId: UUID, memberId: UUID, memberType:String): Future[List[EffMembership]] 
  def getDirectDescendantsByGroup(tenantId: UUID, groupId: UUID): Future[List[EffMembership]] 
  def getDirectAncestorsByMember(tenantId: UUID, memberId: UUID, memberType:String): Future[List[EffMembership]] 
  def getDirectMembership(tenantId: UUID, groupId: UUID, memberId: UUID, memberType:String): Future[Option[EffMembership]]
  def getEffectiveAncestorsByMember(tenantId: UUID, memberId: UUID, memberType:String): Future[List[EffMembership]] 

}

object AncestryDbImpl extends AncestryDb {
  
  val cassandraProvider = DependencyInjector.cassandraProvider
  import cassandraProvider._

  def upsertEffMemberships(mships: List[EffMembership]): Future[List[EffMembership]] = {
    val inserts1 = mships.map { g => DescendantsByGroup.store(g) }
    val inserts2 = mships.map { g => AncestorsByMember.store(g) }
    val inserts = inserts1 ++ inserts2
    // TODO evaluate whether these should be in a batch - they can explode
    Batch.logged.add(inserts.iterator).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => mships
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
    
  }
  
  def removeEffMemberships(mships: List[EffMembership]): Future[List[EffMembership]] = {
    val deletes1 = mships.map { g => DescendantsByGroup.deleteEffMembership(g) }
    val deletes2 = mships.map { g => AncestorsByMember.deleteEffMembership(g) }
    val deletes = deletes1 ++ deletes2
    // TODO evaluate whether these should be in a batch - they can explode
    Batch.logged.add(deletes.iterator).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => mships
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
  }
  
  def getDirectMembership(tenantId: UUID, groupId: UUID, memberId: UUID, memberType:String): Future[Option[EffMembership]] = {
    DescendantsByGroup.getEffMembership(tenantId, groupId, "", memberId, memberType).one()
  }
  
  def getDescendantsByGroup(tenantId: UUID, groupId: UUID): Future[List[EffMembership]] = {
    DescendantsByGroup.getDescendants(tenantId, groupId).fetch()
  }

  def getAncestorsByMember(tenantId: UUID, memberId: UUID, memberType:String): Future[List[EffMembership]] = {
    AncestorsByMember.getAncestors(tenantId, memberId, memberType).fetch()
  }

  def getDirectDescendantsByGroup(tenantId: UUID, groupId: UUID): Future[List[EffMembership]] = {
    DescendantsByGroup.getDescendants(tenantId, groupId, "").fetch()
  }

  def getDirectAncestorsByMember(tenantId: UUID, memberId: UUID, memberType:String): Future[List[EffMembership]] = {
    AncestorsByMember.getAncestors(tenantId, memberId, memberType, "").fetch()
  }

  def getEffectiveAncestorsByMember(tenantId: UUID, memberId: UUID, memberType:String): Future[List[EffMembership]] = {
    AncestorsByMember.getAncestors(tenantId, memberId, memberType).fetch()
  }

  
}