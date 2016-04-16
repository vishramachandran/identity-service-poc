package com.vishr.ids.db

import com.vishr.ids.db.model._
import com.websudos.phantom.connectors.RootConnector
import scala.concurrent.Future
import com.datastax.driver.core.ResultSet
import com.websudos.phantom.dsl._
import com.vishr.ids.common.DependencyInjector
import java.util.UUID
import com.vishr.ids.common.Constants._

trait UserDb {

  def upsertUser(u: User): Future[User]
  def removeUser(u: User): Future[User]
  def getUser(tenantId: UUID, userId: UUID): Future[Option[User]]
  def getUsers(tenantId: UUID, limit:Int = 25): Future[List[User]] 
  def getUser(tenantId: UUID, username: String): Future[Option[User]] 

}

object UserDbImpl extends UserDb {

  val cassandraProvider = DependencyInjector.cassandraProvider
  import cassandraProvider._

  def upsertUser(u: User): Future[User] = {
    val s1 = UserById.store(u)
    val s2 = UserByUsername.store(u)
    Batch.logged.add(s1).add(s2).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => u
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
  }

  def removeUser(u: User): Future[User] = {
    val s1 = UserById.deleteUser(u.tenantId, u.userId)
    val s2 = UserByUsername.deleteUser(u.tenantId, u.username)
    Batch.logged.add(s1).add(s2).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => u
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
  }

  def getUser(tenantId: UUID, userId: UUID): Future[Option[User]] = {
    UserById.getUser(tenantId, userId).one()
  }

  def getUsers(tenantId: UUID, limit:Int = 25): Future[List[User]] = {
      UserById.getUsers(tenantId).limit(limit).fetch()
  }

  def getUser(tenantId: UUID, userIdOrName: String): Future[Option[User]] = {
    if (userIdOrName.matches(UuidRegex)) 
      UserById.getUser(tenantId, UUID.fromString(userIdOrName)).one()
    else
      UserByUsername.getUser(tenantId, userIdOrName).one()
  }


}