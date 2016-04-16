package com.vishr.ids.db.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import net.liftweb.json.DefaultFormats

case class Group(
  tenantId: UUID,
  groupId: UUID,
  groupName: String,
  description: String)

class GroupByName extends CassandraTable[GroupByName, Group] {
  override lazy val tableName = "group_by_name"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object groupName extends StringColumn(this) with PrimaryKey[String]
  object groupId extends UUIDColumn(this)
  object description extends StringColumn(this)

  def fromRow(row: Row): Group = {
    Group(
      tenantId(row),
      groupId(row),
      groupName(row),
      description(row))
  }

}

object GroupByName extends GroupByName {

  def store(u: Group)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.groupId, u.groupId)
      .value(_.groupName, u.groupName)
      .value(_.description, u.description)
  }

  def getGroup(tenantId: UUID, groupName: String)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.groupName eqs groupName)
  }

  def deleteGroup(tenantId: UUID, groupName: String)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs tenantId).and(_.groupName eqs groupName)
  }

  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }

}

class GroupById extends CassandraTable[GroupById, Group] {
  override lazy val tableName = "group_by_id"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object groupId extends UUIDColumn(this) with PrimaryKey[UUID]
  object groupName extends StringColumn(this)
  object description extends StringColumn(this)

  def fromRow(row: Row): Group = {
    Group(
      tenantId(row),
      groupId(row),
      groupName(row),
      description(row))
  }

}

object GroupById extends GroupById {

  def store(u: Group)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.groupId, u.groupId)
      .value(_.groupName, u.groupName)
      .value(_.description, u.description)
  }

  def getGroup(tenantId: UUID, groupId: UUID)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.groupId eqs groupId)
  }

  def getGroups(tenantId: UUID)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId)
  }

  def deleteGroup(tenantId: UUID, groupId: UUID)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs tenantId).and(_.groupId eqs groupId)
  }

  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }

}

