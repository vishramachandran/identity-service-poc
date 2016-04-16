package com.vishr.ids.db.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import net.liftweb.json.DefaultFormats

case class User(
                 tenantId: UUID,
                 userId: UUID,
                 username: String,
                 firstName: String,
                 lastName: String
                 )

class UserById extends CassandraTable[UserById, User] {
  override lazy val tableName = "user_by_id"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object userId extends UUIDColumn(this) with PrimaryKey[UUID]
  object username   extends StringColumn(this)
  object firstName   extends StringColumn(this)
  object lastName   extends StringColumn(this)

  def fromRow(row:Row): User = {
    User(
      tenantId(row),
      userId(row),
      username(row),
      firstName(row),
      lastName(row)
    )
  }

}

object UserById extends UserById {

  def store(u:User)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.userId, u.userId)
      .value(_.username, u.username)
      .value(_.firstName, u.firstName)
      .value(_.lastName, u.lastName)
  }
  
  
  def getUser(tenantId:UUID, userId:UUID)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.userId eqs userId)
  }

  
  def getUsers(tenantId:UUID)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId)
  }
  
  def deleteUser(tenantId:UUID, userId:UUID)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs tenantId).and(_.userId eqs userId)
  }
  
  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }

}


sealed class UserByUsername extends CassandraTable[UserByUsername, User] {
  override lazy val tableName = "user_by_username"
  //  implicit val formats = DefaultFormats
  object tenantId extends UUIDColumn(this) with PartitionKey[UUID]
  object username   extends StringColumn(this) with PrimaryKey[String]
  object userId extends UUIDColumn(this)
  object firstName   extends StringColumn(this)
  object lastName   extends StringColumn(this)

  def fromRow(row:Row): User = {
    User(
      tenantId(row),
      userId(row),
      username(row),
      firstName(row),
      lastName(row)
    )
  }


}

object UserByUsername extends UserByUsername {
  
  def store(u:User)(implicit session: Session, keySpace: KeySpace) = {
    insert
      .value(_.tenantId, u.tenantId)
      .value(_.userId, u.userId)
      .value(_.username, u.username)
      .value(_.firstName, u.firstName)
      .value(_.lastName, u.lastName)
  }
  
  def getUser(tenantId:UUID, username:String)(implicit session: Session, keySpace: KeySpace) = {
    select.where(_.tenantId eqs tenantId).and(_.username eqs username)
  }

  
  def deleteUser(tenantId:UUID, username:String)(implicit session: Session, keySpace: KeySpace) = {
    delete.where(_.tenantId eqs tenantId).and(_.username eqs username)
  }
      
    
  def createTable()(implicit session: Session, keySpace: KeySpace) = {
    create.ifNotExists()
  }
    
}

