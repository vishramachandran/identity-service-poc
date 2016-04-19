package com.vishr.ids.api.model.v1

import com.datastax.driver.core.utils.UUIDs
import java.util.UUID
import com.vishr.ids.common.Constants._
import org.joda.time.DateTime


case class Command(
    commandId: UUID,
    commandType: String,
    issuedByUserId: UUID,
    issuedByUsername: String,
    status: String,   // TODO use enum
    submittedTime:DateTime,
    startedTime:Option[DateTime],
    completedTime: Option[DateTime],
    entityName: Option[String],
    entityId: Option[UUID],
    params: String, // json'ed CommandParams
    errorCode: Option[String] = None,
    errorMessage: Option[String] = None) 
    
    
trait CommandParams {
  def entityId: Option[UUID] = None
  def entityName: Option[String] = None
}

case class UserCreateCommandParams(
  username: String,
  firstName: String,
  lastName: String) extends CommandParams

case class UserUpdateCommandParams(
    user: User) extends CommandParams {
  override val entityId = Some(user.userId)
}

case class UserDeleteCommandParams(
    userIdOrName: String) extends CommandParams {
  override val entityId = if (userIdOrName.matches(UuidRegex)) Some(UUID.fromString(userIdOrName)) else None
  override val entityName = if (!userIdOrName.matches(UuidRegex)) Some(userIdOrName) else None
}

case class GroupCreateCommandParams(
  groupName: String,
  description: String) extends CommandParams

case class GroupUpdateCommandParams(
    group: Group) extends CommandParams {
  override val entityId = Some(group.groupId)
}

case class GroupDeleteCommandParams(
    groupIdOrName: String) extends CommandParams {
  override val entityId = if (groupIdOrName.matches(UuidRegex)) Some(UUID.fromString(groupIdOrName)) else None
  override val entityName = if (!groupIdOrName.matches(UuidRegex)) Some(groupIdOrName) else None
}

case class MembershipCreateParams(
    groupIdOrName: String,
    memberIdOrName: String,
    memberType: String) extends CommandParams {
  override val entityId = if (groupIdOrName.matches(UuidRegex)) Some(UUID.fromString(groupIdOrName)) else None
  override val entityName = if (!groupIdOrName.matches(UuidRegex)) Some(groupIdOrName) else None
}

case class MembershipDeleteParams(
    groupIdOrName: String,
    memberIdOrName: String,
    memberType: String) extends CommandParams {
  override val entityId = if (groupIdOrName.matches(UuidRegex)) Some(UUID.fromString(groupIdOrName)) else None
  override val entityName = if (!groupIdOrName.matches(UuidRegex)) Some(groupIdOrName) else None
}


