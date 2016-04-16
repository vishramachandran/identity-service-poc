package com.vishr.ids.common

import com.vishr.ids.db.model.CommandById
import org.joda.time.DateTime

object DbToApiConversions {
  
  def convert(c:com.vishr.ids.db.model.Command) = {
    com.vishr.ids.api.model.v1.Command(
        commandId = c.commandId,
        commandType = c.commandType,
        issuedByUserId = c.issuedByUserId,
        issuedByUsername = c.issuedByUsername,
        status = c.status,
        entityName = c.entityName,
        entityId = c.entityId,
        params = c.params,
        submittedTime = c.submittedTime,
        startedTime = c.startedTime,
        completedTime = c.completedTime,
        errorCode = c.errorCode,
        errorMessage = c.errorMessage
    )
  }
  
  def convert(u:com.vishr.ids.db.model.User) = {
    com.vishr.ids.api.model.v1.User(
        userId = u.userId,
        username = u.username,
        firstName = u.firstName,
        lastName = u.lastName)
  }

  def convert(u:com.vishr.ids.db.model.Group) = {
    com.vishr.ids.api.model.v1.Group(
        groupId = u.groupId,
        groupName = u.groupName,
        description = u.description)
  }

  
  def convert(m:com.vishr.ids.db.model.EffMembership) = {
    com.vishr.ids.api.model.v1.Membership(
        groupId = m.groupId,
        groupName = m.groupName,
        memberId = m.memberId,
        memberType = m.memberType,
        memberName = m.memberName)
        
  }
}