package com.vishr.ids.test
import com.vishr.ids.clients._
import com.vishr.ids.api.model.v1._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import com.vishr.ids.clients.CommandClient
import com.vishr.ids.clients.system
import com.vishr.ids.clients.GroupClient
import com.vishr.ids.clients.UserClient

object SimpleFunctionalTest extends App {

  import system._
    
  val baseUrl = "http://localhost:8000"
  val userClient = new UserClient(baseUrl)
  val groupClient = new GroupClient(baseUrl)
  val commandClient = new CommandClient(baseUrl)
  
  val users = for { i<- 1 to 2 } yield {
    val u = UserCreateCommandParams("u"+i,"firstName"+i,"lastName"+i)
    println("posting: " + u)
    userClient.createUser(u) flatMap { c=> 
      commandClient.waitCommand(c, 1 seconds, 6)
    }
  } 
  
  println("queued users")
  Await.result(Future.sequence(users), 20 seconds) 
  

  val groups = for { i<- 1 to 6 } yield {
    val g = GroupCreateCommandParams("g"+i,"description"+i)
    groupClient.createGroup(g) flatMap { c=> 
      commandClient.waitCommand(c, 1 second, 6) 
    }
  } 
  
  println("queued groups")
  Await.result(Future.sequence(groups), 20 seconds) 
  
  val members = List( 
      ("g3","g4"),
      ("g1","u1"),
      ("g3","g2"),
      ("g2","g1"),
      ("g4","g1"),
      ("g4","u2"),
      ("g6","g5"),
      ("g5","g4")
      )

  /*
   *   							g3<-----g2 <------ g1<---u1
   *   							^                  |
   *   							|                  |
   * g6<---g5<----- g4<-----------------
   *   							^  
   *   							| 
   *   							u2 
   *     
   */
  val memberships = members map { m =>
    val memberType =  if (m._2.startsWith("u")) "User" else "Group"
    val mShip = MembershipCreateParams(m._1,m._2, memberType)
    groupClient.addGroupMember(mShip) flatMap { c=> 
      commandClient.waitCommand(c, 2 seconds, 10) 
    }
  } 

  println("queued membership")
  Await.result(Future.sequence(memberships), 20 seconds) 
  
  
  val checkU1EffMemberships = userClient.getEffMemberships("u1") map { mShips =>
    if (mShips.length != 6) throw new RuntimeException("Unexpected number of members for u1: " + mShips.length)
    if (!mShips.exists(_.groupName == "g1")) throw new RuntimeException("u1 not member of g1!!")
    if (!mShips.exists(_.groupName == "g2")) throw new RuntimeException("u1 not member of g2!!")
    if (!mShips.exists(_.groupName == "g3")) throw new RuntimeException("u1 not member of g3!!")
    if (!mShips.exists(_.groupName == "g4")) throw new RuntimeException("u1 not member of g4!!")
    if (!mShips.exists(_.groupName == "g5")) throw new RuntimeException("u1 not member of g5!!")
    if (!mShips.exists(_.groupName == "g6")) throw new RuntimeException("u1 not member of g6!!")
    Unit
  }
  
  Await.result(checkU1EffMemberships, 20 seconds) 
  
  val checkU2EffMemberships = userClient.getEffMemberships("u2") map { mShips =>
    if (mShips.length != 4) throw new RuntimeException("Unexpected number of members for u2: " + mShips.length)
    if (!mShips.exists(_.groupName == "g3")) throw new RuntimeException("u2 not member of g3!!")
    if (!mShips.exists(_.groupName == "g4")) throw new RuntimeException("u2 not member of g4!!")
    if (!mShips.exists(_.groupName == "g5")) throw new RuntimeException("u2 not member of g5!!")
    if (!mShips.exists(_.groupName == "g6")) throw new RuntimeException("u2 not member of g6!!")
    Unit
  }
  
  Await.result(checkU2EffMemberships, 20 seconds) 
  
  
  val removeMemberships = members map { m =>
      val memberType =  if (m._2.startsWith("u")) "User" else "Group"
      val mShip = MembershipDeleteParams(m._1,m._2, memberType)
      groupClient.removeGroupMember(mShip) flatMap { c=> 
        commandClient.waitCommand(c, 2 seconds, 10) 
      }
  } 

  println("queued remove membership")
  Await.result(Future.sequence(removeMemberships), 20 seconds) 

  
  val checkU1EffMemberships2 = userClient.getEffMemberships("u1") map { mShips =>
    if (mShips.length != 0) throw new RuntimeException("Unexpected number of members for u1: " + mShips.length)
    Unit
  }
  
  Await.result(checkU1EffMemberships2, 20 seconds) 
  
  val checkU2EffMemberships2 = userClient.getEffMemberships("u2") map { mShips =>
    if (mShips.length != 0) throw new RuntimeException("Unexpected number of members for u2: " + mShips.length)
    Unit
  }

  Await.result(checkU2EffMemberships2, 20 seconds) 
  
  val usersDelete = for { i<- 1 to 2 } yield {
    userClient.deleteUser("u"+i) flatMap { c=> 
      commandClient.waitCommand(c, 1 seconds, 6)
    }
  } 
  
  println("queued user deletes")
  Await.result(Future.sequence(usersDelete), 20 seconds) 
  

  val groupsDelete = for { i<- 1 to 6 } yield {
    groupClient.deleteGroup("g"+i) flatMap { c=> 
      commandClient.waitCommand(c, 1 second, 6) 
    }
  } 
  
  println("queued group deletes")
  Await.result(Future.sequence(groupsDelete), 20 seconds) 
  
  
  val afterUserDelete = userClient.getUsers() map { users =>
    if (users.length != 0) throw new RuntimeException("Unexpected number of users: " + users.length)
    Unit
  }

  Await.result(afterUserDelete, 20 seconds) 
  
  val afterGroupDelete = groupClient.getGroups() map { groups =>
    if (groups.length != 0) throw new RuntimeException("Unexpected number of groups: " + groups.length)
    Unit
  }

  Await.result(afterGroupDelete, 20 seconds) 
  
  System.exit(0)
  
}