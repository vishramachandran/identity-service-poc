package com.vishr.ids.db

import com.websudos.phantom.connectors.ContactPoints
import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.connectors.ContactPoint
import com.datastax.driver.core.Session


trait CassandraProvider {
  implicit val keySpace:KeySpace = KeySpace("vidm")
  implicit val session:Session
}

object LocalCassandraProvider extends CassandraProvider {

  val connector = ContactPoint.local.keySpace(keySpace.name)
  implicit val session = connector.session
  
}
