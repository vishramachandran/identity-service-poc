package com.vishr.ids.db.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import net.liftweb.json.DefaultFormats
import org.joda.time.DateTime
import java.util.UUID

//sealed trait CommandStatus { def name: String }
//case object Issued extends CommandStatus    { val name = "Issued" } 
//case object Running extends CommandStatus    { val name = "Running" } 
//case object Successful extends CommandStatus { val name = "Successful" } 
//case object Failed extends CommandStatus     { val name = "Failed" } 

case class Command(
    tenantId: UUID,
    commandId: UUID,
    commandType: String,
    issuedByUserId: UUID,
    issuedByUsername: String,
    entityName: Option[String] = None,
    entityId: Option[UUID] = None,
    status: String,
    params: String,
    submittedTime: DateTime = DateTime.now(),
    startedTime: Option[DateTime] = None,
    completedTime: Option[DateTime] = None,
    parentCommandId: Option[UUID] = None,
    errorCode: Option[String] = None,
    errorMessage: Option[String] = None) {

  def failed(errorCode: String, errorMessage: String, newEntityId: Option[UUID], newEntityName: Option[String]) = {
    copy(
      status = "Failure",
      completedTime = Some(DateTime.now()),
      errorCode = Some(errorCode),
      errorMessage = Some(errorMessage),
      entityId = entityId.orElse(newEntityId),
      entityName = entityName.orElse(newEntityName))
  }

  def succeeded(newEntityId: Option[UUID], newEntityName: Option[String]) = {
    copy(status = "Success",
      completedTime = Some(DateTime.now()),
      entityId = entityId.orElse(newEntityId),
      entityName = entityName.orElse(newEntityName))
  }
}

class CommandById extends CassandraTable[CommandById, Command] {
  override lazy val tableName = "command_by_id"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object commandId extends TimeUUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Descending
  object commandType extends StringColumn(this)
  object issuedByUserId extends UUIDColumn(this)
  object issuedByUsername extends StringColumn(this)
  object entityName extends OptionalStringColumn(this)
  object entityId extends OptionalUUIDColumn(this)
  object status extends StringColumn(this)
  object params extends StringColumn(this)
  object submittedTime extends DateTimeColumn(this)
  object startedTime extends OptionalDateTimeColumn(this)
  object completedTime extends OptionalDateTimeColumn(this)
  object parentCommandId extends OptionalUUIDColumn(this)
  object errorCode extends OptionalStringColumn(this)
  object errorMessage extends OptionalStringColumn(this)

  def fromRow(row: Row): Command = {
    Command(
      tenantId(row),
      commandId(row),
      commandType(row),
      issuedByUserId(row),
      issuedByUsername(row),
      entityName(row),
      entityId(row),
      status(row),
      params(row),
      submittedTime(row),
      startedTime(row),
      completedTime(row),
      parentCommandId(row),
      errorCode(row),
      errorMessage(row))
  }

}

object CommandById extends CommandById {

  def store(u: Command)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.commandId, u.commandId)
      .value(_.commandType, u.commandType)
      .value(_.entityId, u.entityId)
      .value(_.entityName, u.entityName)
      .value(_.issuedByUserId, u.issuedByUserId)
      .value(_.issuedByUsername, u.issuedByUsername)
      .value(_.status, u.status)
      .value(_.params, u.params)
      .value(_.submittedTime, u.submittedTime)
      .value(_.startedTime, u.startedTime)
      .value(_.completedTime, u.completedTime)
      .value(_.parentCommandId, u.parentCommandId)
      .value(_.errorCode, u.errorCode)
      .value(_.errorMessage, u.errorMessage)
  }

  def getCommands(tenantId: UUID, limit: Int)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).limit(limit)
  }

  def getCommand(tenantId: UUID, commandId: UUID)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.commandId eqs commandId)
  }

  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }

}
    
  
