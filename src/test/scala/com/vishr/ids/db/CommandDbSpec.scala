package com.vishr.ids.db

import com.vishr.ids.db.model.Command
import scala.concurrent.ExecutionContext.Implicits.global
import com.websudos.util.testing._
import com.vishr.ids.common.Constants
import java.util.UUID
import com.datastax.driver.core.utils.UUIDs
import com.vishr.ids.api.model.v1.UserCreateCommandParams
import org.joda.time.DateTime

class CommandDbSpec extends DbSpecBase {

  def keySpace = LocalCassandraProvider.keySpace
  
  val commandDb : CommandDb = CommandDbImpl 

  it should "store command" in {

    val c : Command = 
       com.vishr.ids.db.model.Command(
        tenantId = Constants.zero, 
        commandId = UUIDs.timeBased(), 
        commandType = UserCreateCommandParams.getClass.getSimpleName, 
        issuedByUserId = Constants.zero,
        issuedByUsername = "TODO",
        entityId = Some(UUID.randomUUID()),
        status = "Issued",
        submittedTime = DateTime.now(),
        params= "params")

      
    val chain = for {
      store <- commandDb.upsertCommand(c)
      get <- commandDb.getCommand(c.tenantId, c.commandId)
    } yield get

  chain.successful {
      result => {
        result.isDefined shouldEqual true
        result.get shouldEqual c
      }
    }    
  }
}
