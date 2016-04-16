package com.vishr.ids.db.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import net.liftweb.json.DefaultFormats

case class EffMembership(
  tenantId: UUID,
  groupId: UUID,
  path: String,
  memberId: UUID,
  memberType: String, // user or group TODO replace with better enum
  groupName: String,
  memberName: String)

class DescendantsByGroup extends CassandraTable[DescendantsByGroup, EffMembership] {
  override lazy val tableName = "descendants_by_group"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object groupId extends UUIDColumn(this) with PartitionKey[UUID]
  object path    extends StringColumn(this) with PrimaryKey[String]
  object memberId extends UUIDColumn(this) with PrimaryKey[UUID]
  object memberType extends StringColumn(this) with PrimaryKey[String]
  object groupName extends StringColumn(this)
  object memberName extends StringColumn(this)

  def fromRow(row: Row): EffMembership = {
    EffMembership(
      tenantId(row),
      groupId(row),
      path(row),
      memberId(row),
      memberType(row),
      groupName(row),
      memberName(row))
  }

}

object DescendantsByGroup extends DescendantsByGroup {

  def store(u: EffMembership)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.groupId, u.groupId)
      .value(_.path, u.path)
      .value(_.memberId, u.memberId)
      .value(_.memberType, u.memberType)
      .value(_.groupName, u.groupName)
      .value(_.memberName, u.memberName)
  }

  def getDescendants(tenantId: UUID, groupId: UUID)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.groupId eqs groupId)
  }

  def getDescendants(tenantId: UUID, groupId: UUID, path:String)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.groupId eqs groupId).and(_.path eqs path)
  }

  def deleteDescendants(tenantId: UUID, groupId: UUID)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs tenantId).and(_.groupId eqs groupId)
  }

  def deleteEffMembership(m:EffMembership)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs m.tenantId).and(_.groupId eqs m.groupId).and(_.path eqs m.path).and(_.memberId eqs m.memberId).and(_.memberType eqs m.memberType)
  }

  def getEffMembership(tenantId: UUID, groupId: UUID, path:String, memberId: UUID, memberType:String)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.groupId eqs groupId).and(_.path eqs path).and(_.memberId eqs memberId).and(_.memberType eqs memberType)
  }

  
  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }

}


class AncestorsByMember extends CassandraTable[AncestorsByMember, EffMembership] {
  override lazy val tableName = "ancestors_by_member"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object memberId extends UUIDColumn(this) with PartitionKey[UUID]
  object memberType extends StringColumn(this) with PartitionKey[String]
  object path    extends StringColumn(this) with PrimaryKey[String]
  object groupId extends UUIDColumn(this) with PrimaryKey[UUID]
  object groupName extends StringColumn(this)
  object memberName extends StringColumn(this)

  def fromRow(row: Row): EffMembership = {
    EffMembership(
      tenantId(row),
      groupId(row),
      path(row),
      memberId(row),
      memberType(row),
      groupName(row),
      memberName(row))
  }
}


object AncestorsByMember extends AncestorsByMember {

  def store(u: EffMembership)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.groupId, u.groupId)
      .value(_.memberId, u.memberId)
      .value(_.memberType, u.memberType)
      .value(_.path, u.path)
      .value(_.groupName, u.groupName)
      .value(_.memberName, u.memberName)
  }

  def getAncestors(tenantId: UUID, memberId: UUID, memberType: String)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.memberId eqs memberId).and(_.memberType eqs memberType)
  }

  def getAncestors(tenantId: UUID, memberId: UUID, memberType: String, path:String)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.memberId eqs memberId).and(_.memberType eqs memberType).and(_.path eqs path)
  }

  def deleteEffMembership(m:EffMembership)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs m.tenantId).and(_.groupId eqs m.groupId).and(_.memberId eqs m.memberId).and(_.memberType eqs m.memberType).and(_.path eqs m.path)
  }

  def deleteAncestors(tenantId: UUID, memberId: UUID, memberType: String)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs tenantId).and(_.memberId eqs memberId).and(_.memberType eqs memberType)
  }

  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }

}

