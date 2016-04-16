package com.vishr.ids.db

import com.vishr.ids.db.model._
import com.websudos.phantom.connectors.RootConnector
import scala.concurrent.Future
import com.datastax.driver.core.ResultSet
import com.websudos.phantom.dsl._
import com.vishr.ids.common.DependencyInjector
import java.util.UUID


trait CommandDb {

  def upsertCommand(u: Command): Future[Command]
  def getRecentCommands(tenantId:UUID): Future[List[Command]]
  def getCommand(tenantId:UUID,commandId:UUID): Future[Option[Command]]

}


object CommandDbImpl extends CommandDb {

  val cassandraProvider = DependencyInjector.cassandraProvider
  import cassandraProvider._

  def upsertCommand(u: Command): Future[Command] = {
    CommandById.store(u).future() map { res: ResultSet =>
      res.wasApplied() match {
        case true => u
        case false => throw new RuntimeException(res.getExecutionInfo.toString()) // TODO log error
      }
    }
  }
  
  def getRecentCommands(tenantId:UUID): Future[List[Command]] = {
    CommandById.getCommands(tenantId, 25).fetch()
  }

  def getCommand(tenantId:UUID,commandId:UUID): Future[Option[Command]] = {
    CommandById.getCommand(tenantId, commandId).one()
  }


}