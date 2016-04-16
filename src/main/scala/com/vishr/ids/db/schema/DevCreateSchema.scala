package com.vishr.ids.db.schema

import com.vishr.ids.db.model.UserById
import com.vishr.ids.db.model.UserByUsername
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import com.vishr.ids.db.model.GroupById
import com.vishr.ids.db.model.GroupByName
import com.vishr.ids.db.model.CommandById
import com.vishr.ids.db.LocalCassandraProvider
import com.vishr.ids.db.model.DescendantsByGroup
import com.vishr.ids.db.model.AncestorsByMember

object DevCreateSchema extends App {

  createSchema()
  System.exit(0)

  
  def createSchema() : Unit = {
    val cassandraProvider = LocalCassandraProvider
    import cassandraProvider._
  
    val dropKeyspace = session.execute("DROP KEYSPACE vidm")
    val createKeyspace = session.execute("CREATE KEYSPACE IF NOT EXISTS vidm WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }")
    
    val f1 = UserById.createTable().execute()
    val f2 = UserByUsername.createTable().execute()
    val f3 = DescendantsByGroup.createTable().execute()
    val f4 = AncestorsByMember.createTable().execute()
    val f7 = GroupById.createTable().execute()
    val f8 = GroupByName.createTable().execute()
    val f9 = CommandById.createTable().execute()
  
    session.close()
    
  }
  
}