

package com.vishr.ids.clients

import scala.concurrent.ExecutionContext.Implicits.global
import com.vishr.ids.api.model.v1.UserCreateCommandParams
import net.liftweb.json._
import net.liftweb.json.Serialization.{ read, write }
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.LiftFormats

class UserClient(val baseUrl: String) {

  import LiftFormats._
  import system._

  def createUser(p: UserCreateCommandParams) = {

    wsClient
      .url(baseUrl + "/users/create")
      .withHeaders("Content-Type" -> "application/json")
      .post(write(p))
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      }
  }

  def deleteUser(userIdOrName: String) = {

    wsClient
      .url(baseUrl + "/users/" + userIdOrName + "/delete")
      .withHeaders("Content-Type" -> "application/json")
      .post("")
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      }
  }

  def getUser(userIdOrName: String) = {

    wsClient
      .url(baseUrl + "/users/" + userIdOrName)
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[User](wsResponse.body)
      }
  }

  def getUsers() = {

    wsClient
      .url(baseUrl + "/users")
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[List[User]](wsResponse.body)
      }
  }

  def getEffMemberships(userIdOrName: String) = {

    wsClient
      .url(baseUrl + "/users/" + userIdOrName + "/effectiveMembership")
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[List[Membership]](wsResponse.body)
      }
  }

}