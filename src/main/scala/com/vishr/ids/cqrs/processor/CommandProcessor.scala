package com.vishr.ids.cqrs.processor

import com.vishr.ids.db.model.Command
import com.vishr.ids.api.model.v1._
import net.liftweb.json._
import net.liftweb.json.Serialization.read
import com.vishr.ids.common.LiftFormats
import com.vishr.ids.api.model.v1.CommandParams
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration._
import com.vishr.ids.common.DependencyInjector
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime

trait CommandProcessor {
  def processCommand(cmd: Command): Unit
}

object CommandProcessorImpl extends CommandProcessor {

  import UserCommandProcessor._
  import GroupCommandProcessor._
  import AddMembershipCommandProcessor._
  import RemoveMembershipCommandProcessor._

  val logger = LoggerFactory.getLogger(this.getClass)
  val commandDb = DependencyInjector.commandDb

  def processCommand(cmd: Command): Unit = {

    logger.info("Processing command: " + cmd)
    implicit val formats = LiftFormats.formats
    val params = read[CommandParams](cmd.params)

    // check if command has already been processed, so it is idempotent
    // handle case where we crashed while command is being processed, and message is redelivered
    val commandExecFuture = commandDb.getCommand(cmd.tenantId, cmd.commandId) map { cmdInDb: Option[Command] =>
      cmdInDb match {
        case None =>
          // possible that we are reading from difft cass node, 
          // or there is a race condition between kafka and cass.
          // proceed anyway
          Unit
        case Some(c) =>
          if (c.status == "Success" || c.status == "Failure")
            throw new RuntimeException("Command " + c + " was redelivered. Skipping")
          Unit
      }
    } flatMap { _ =>
      commandDb.upsertCommand(cmd.copy(status = "Running", startedTime = Some(DateTime.now())))
    } flatMap { c: Command =>

     params match {
        case p: UserCreateCommandParams => createUser(c, p)
        case p: UserUpdateCommandParams => updateUser(c, p)
        case p: UserDeleteCommandParams => deleteUser(c, p)
        case p: GroupCreateCommandParams => createGroup(c, p)
        case p: GroupUpdateCommandParams => updateGroup(c, p)
        case p: GroupDeleteCommandParams => deleteGroup(c, p)
        case p: MembershipCreateParams => addMember(c, p)
        case p: MembershipDeleteParams => deleteMember(c, p)
      }
    }

    try {
      // TODO see how we can avoid this blocking call 
      // and commit kafka message asynchronously
      Await.result(commandExecFuture, 5 minutes)
      logger.info("Successfully processed " + cmd)
    } catch {
      case ex: Exception =>
        logger.error("ALERT: Command " + cmd + " ran into an exception", ex)
    }

  }

}