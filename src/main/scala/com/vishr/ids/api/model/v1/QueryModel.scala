package com.vishr.ids.api.model.v1

import java.util.UUID
import com.datastax.driver.core.utils.UUIDs

case class User(
    userId: UUID,
    username: String,
    firstName: String,
    lastName: String
)

case class Group(
    groupId: UUID,
    groupName: String,
    description: String
)

//sealed trait MemberType { def name: String }
//case object UserMember extends MemberType { val name = "User" } 
//case object GroupMember extends MemberType { val name = "Group" } 

case class Membership(
  groupId: UUID,
  groupName: String,
  memberId: UUID,
  memberType: String,  // TODOuse enumeration
  memberName: String
)

 
case class ApiError(
    httpResponseCode: String,
    apiErrorCode: String,
    message: String,
    action: Option[String]
)

