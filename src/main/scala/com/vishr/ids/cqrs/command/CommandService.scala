package com.vishr.ids.cqrs.command

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import com.datastax.driver.core.utils.UUIDs
import com.vishr.ids.api.model.v1.Command
import com.vishr.ids.api.model.v1.CommandParams
import com.vishr.ids.common.Constants
import net.liftweb.json.Serialization.write
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.common.LiftFormats
import com.vishr.ids.common.DbToApiConversions
import org.joda.time.DateTime

trait CommandService {
  
  def getRecentCommands(tenantId:UUID) : Future[List[Command]] 
  def queueCommand(tenantId: UUID, params:CommandParams)  : Future[Command] 
  def getCommand(tenantId:UUID,commandId:UUID) : Future[Option[Command]] 
}

object CommandServiceImpl extends CommandService {
  
  val logger = LoggerFactory.getLogger(this.getClass)
  def commandDb = DependencyInjector.commandDb
  def messagingDispatcher = DependencyInjector.messagingDispatcher
  
  import com.vishr.ids.common.DbToApiConversions._
  import com.vishr.ids.common.LiftFormats._
  
  def getRecentCommands(tenantId:UUID) : Future[List[Command]]  =
      commandDb.getRecentCommands(tenantId) map ( _ map (convert(_)))

  def getCommand(tenantId:UUID,commandId:UUID) : Future[Option[Command]]  = {
    logger.debug("Got GET for " + tenantId + " " + commandId)
    commandDb.getCommand(tenantId, commandId) map ( _ map (convert(_)))
  }
      
      
  def queueCommand(tenantId: UUID, params:CommandParams)  : Future[Command] = {

    val id = UUIDs.timeBased();
    val submittedTime = new DateTime(UUIDs.unixTimestamp(id))
    val dbCmd = com.vishr.ids.db.model.Command(
        tenantId = tenantId, 
        commandId = id, 
        commandType = params.getClass.getSimpleName, 
        issuedByUserId = Constants.zero,
        issuedByUsername = "TODO",
        entityId = params.entityId,
        status = "Issued",
        submittedTime = submittedTime,
        params= write(params))
    messagingDispatcher.send(tenantId.toString(), write(dbCmd)) flatMap { _ =>
      commandDb.upsertCommand(dbCmd) 
    } map { cmd => 
      convert(cmd)
    }
  }
}