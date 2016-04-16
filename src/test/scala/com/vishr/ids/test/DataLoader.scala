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

object DataLoader extends App {

  import system._

  val baseUrl = "http://localhost:8000"
  val userClient = new UserClient(baseUrl)
  val groupClient = new GroupClient(baseUrl)
  val commandClient = new CommandClient(baseUrl)

  val start = System.currentTimeMillis()
  
  for { i <- 0L until 10000L } {

    println("=========== Start Round " + i + " =======================")

    val users = for { j <- i * 8 until i * 8 + 8 } yield {
      val u = UserCreateCommandParams("u" + j, "firstName" + j, "lastName" + j)
      userClient.createUser(u)
    }

    val groups = for { j <- i * 4 until i * 4 + 4 } yield {
      val g = GroupCreateCommandParams("g" + j, "description" + j)
      groupClient.createGroup(g)
    }

    Await.result(Future.sequence(users ++ groups), 10 minutes)

    val g = i * 4
    val u = i * 8

    val members = List(
      ("g" + (g + 0), "g" + (g + 1)),
      ("g" + (g + 1), "g" + (g + 2)),
      ("g" + (g + 2), "g" + (g + 3)),
      ("g" + (g + 0), "u" + (u + 0)),
      ("g" + (g + 0), "u" + (u + 1)),
      ("g" + (g + 1), "u" + (u + 2)),
      ("g" + (g + 1), "u" + (u + 3)),
      ("g" + (g + 2), "u" + (u + 4)),
      ("g" + (g + 2), "u" + (u + 5)),
      ("g" + (g + 3), "u" + (u + 6)),
      ("g" + (g + 3), "u" + (u + 7)))

    val memberships = members map { m =>
      val memberType = if (m._2.startsWith("u")) "User" else "Group"
      val mShip = MembershipCreateParams(m._1, m._2, memberType)
      groupClient.addGroupMember(mShip)
    }

    Await.result(Future.sequence(memberships), 10 minutes)

    println("=========== End Round "+i+" =======================")

  }

  val end = System.currentTimeMillis()
  println("Time Taken in millis:" + (start-end))
  System.exit(0)
}