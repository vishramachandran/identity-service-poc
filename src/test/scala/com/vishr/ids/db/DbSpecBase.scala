package com.vishr.ids.db

import com.websudos.phantom.testkit.suites.CassandraFlatSpec
import com.vishr.ids.db.schema.DevCreateSchema

trait DbSpecBase extends CassandraFlatSpec {
 
   override def beforeAll(): Unit = {
     super.beforeAll()
     DevCreateSchema.createSchema()
   }

   override def afterAll(): Unit = {
     super.afterAll()
//     DatabaseService.cleanup()
   }
}

