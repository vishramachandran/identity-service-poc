

package com.vishr.ids.clients

import scala.concurrent.ExecutionContext.Implicits.global
import net.liftweb.json._
import net.liftweb.json.Serialization.{ read, write }
import com.vishr.ids.api.model.v1._

class GroupClient(val baseUrl: String) {

  import com.vishr.ids.common.LiftFormats._
  import system._

  def createGroup(p: GroupCreateCommandParams) = {

    wsClient
      .url(baseUrl + "/groups/create")
      .withHeaders("Content-Type" -> "application/json")
      .post(write(p))
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      }
  }

  def deleteGroup(groupIdOrName: String) = {

    wsClient
      .url(baseUrl + "/groups/" + groupIdOrName + "/delete")
      .withHeaders("Content-Type" -> "application/json")
      .post("")
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      }
  }

  def getGroup(groupIdOrName: String) = {

    wsClient
      .url(baseUrl + "/groups/" + groupIdOrName)
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Group](wsResponse.body)
      }
  }

  def getGroups() = {

    wsClient
      .url(baseUrl + "/groups")
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[List[Group]](wsResponse.body)
      }
  }

  
  def addGroupMember(p: MembershipCreateParams) = {

    wsClient
      .url(baseUrl + "/groups/" + p.groupIdOrName + "/addMember")
      .withHeaders("Content-Type" -> "application/json")
      .post(write(p))
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      }
  }

  def removeGroupMember(p: MembershipDeleteParams) = {

    wsClient
      .url(baseUrl + "/groups/" + p.groupIdOrName + "/removeMember")
      .withHeaders("Content-Type" -> "application/json")
      .post(write(p))
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[Command](wsResponse.body)
      }
  }

  def getMembers(groupIdOrName: String) = {

    wsClient
      .url(baseUrl + "/users/" + groupIdOrName + "/members")
      .get()
      .map { wsResponse =>
        if (!(200 to 299).contains(wsResponse.status)) {
          throw new RuntimeException(s"Received unexpected status ${wsResponse.status} : ${wsResponse.body}")
        }
        read[List[Membership]](wsResponse.body)
      }
  }

}