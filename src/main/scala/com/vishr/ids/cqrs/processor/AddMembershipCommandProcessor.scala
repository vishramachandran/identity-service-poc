package com.vishr.ids.cqrs.processor

import com.vishr.ids.db.model.Command
import com.vishr.ids.api.model.v1.MembershipCreateParams
import com.vishr.ids.api.model.v1.MembershipDeleteParams
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.db.model.EffMembership
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import com.vishr.ids.db.model.EffMembership
import com.vishr.ids.db.model.EffMembership
import com.vishr.ids.db.model.User
import com.vishr.ids.db.model.Group
import com.vishr.ids.db.model.EffMembership
import java.util.UUID
import com.vishr.ids.db.model.EffMembership
import com.vishr.ids.db.model.EffMembership
import org.slf4j.LoggerFactory

object AddMembershipCommandProcessor {

  val logger = LoggerFactory.getLogger(this.getClass)

  def ancestryDb = DependencyInjector.ancestryDb
  def groupDb = DependencyInjector.groupDb
  def userDb = DependencyInjector.userDb
  def commandDb = DependencyInjector.commandDb

  def addMember(cmd: Command, params: MembershipCreateParams) = {
    
    logger.info("Got add Member command: " + params)
    
    /*
     * Procedure add member m to group g
     * 1. Validate group and member 
     * 2. Ensure membership not already exists. Nothing to do if this is the case
     * 3. Ensure reverse membership does not exist. Cycles not allowed
     * 4. For each ancestor a of g including g
     *       For each descendant d of m including m
     *            add effective membership for a<---d with path g,m
     */

    case class MemCreateParams(groupId:UUID, groupName:String, memberId:UUID, memberName:String,memberType:String)

    val grp = groupDb.getGroup(cmd.tenantId, params.groupIdOrName) 
    val mmbrGroup = params.memberType match {
      case "Group" => groupDb.getGroup(cmd.tenantId, params.memberIdOrName)
      case "User" => Future(None)
    }
    val mmbrUser = params.memberType match {
      case "User" => userDb.getUser(cmd.tenantId, params.memberIdOrName)
      case "Group" => Future(None)
    }

    val stage1 = Future.sequence(List(grp, mmbrGroup, mmbrUser))

    val stage2 = stage1 flatMap { res =>
      
      val group = res(0).asInstanceOf[Option[Group]]
      val memberGroup = res(1).asInstanceOf[Option[Group]]
      val memberUser = res(2).asInstanceOf[Option[User]]
      
      if (group.isEmpty) 
        throw new CommandFailedException(cmd.failed("BadRequest", "No such group exists", None, Some(params.groupIdOrName)))
      
      if (memberGroup.isEmpty && memberUser.isEmpty) 
        throw new CommandFailedException(cmd.failed("BadRequest", "No such member exists", group.map(_.groupId), group.map(_.groupName)))

      val mParams = MemCreateParams(group.get.groupId, 
                group.get.groupName,
                memberGroup.map(_.groupId).getOrElse(memberUser.get.userId), 
                memberGroup.map(_.groupName).getOrElse(memberUser.get.username),
                params.memberType) 

      val existingMembership = ancestryDb.getDirectMembership(cmd.tenantId, mParams.groupId, mParams.memberId, mParams.memberType)
      val ancestors = ancestryDb.getAncestorsByMember(cmd.tenantId, mParams.groupId, "Group")
      val descendants = params.memberType match {
        case "User" => Future(List[EffMembership]())
        case "Group" => ancestryDb.getDescendantsByGroup(cmd.tenantId, mParams.memberId)
      }
        
      Future.sequence(List(existingMembership, ancestors, descendants, Future(mParams)))

    }
    
    
    val stage3 :Future[Command] = stage2 flatMap { res =>
      
      val existingMembership = res(0).asInstanceOf[Option[EffMembership]]
      val ancestors = res(1).asInstanceOf[List[EffMembership]]
      val descendants = res(2).asInstanceOf[List[EffMembership]]
      val mParams = res(3).asInstanceOf[MemCreateParams]
      
      logger.info("Basic futures completed successfully")
      
      if (existingMembership.isDefined) 
        throw new CommandFailedException(cmd.failed("BadRequest", "Direct membership already exists. Nothing to do", Some(mParams.groupId), Some(mParams.groupName)))
      
      val reverseMembership = ancestors.filter(params.memberType == "Group" && _.groupId == mParams.memberId)
      
      if (!reverseMembership.isEmpty) 
        throw new CommandFailedException(cmd.failed("BadRequest", "Reverse membership already exists. Cycles not allowed", Some(mParams.groupId), Some(mParams.groupName)))

      logger.info("Validations complete.")

      // Now do the operation
      logger.debug("Ancestors: " + ancestors)
      logger.debug("Descendants: " + descendants)

      val direct =
          EffMembership(
              cmd.tenantId,
              mParams.groupId,
              "", 
              mParams.memberId,
              mParams.memberType,
              mParams.groupName,
              mParams.memberName)
        
      val effMemberships = for {
          a <- direct :: ancestors
          d <- direct :: descendants
      } yield {
          EffMembership(
              cmd.tenantId,
              a.groupId,
              combinePath(a,d), 
              d.memberId,
              d.memberType,
              a.groupName,
              d.memberName)
      }
          
      logger.debug("Evaluated Effective Memberships: " + effMemberships)
      
      ancestryDb.upsertEffMemberships(effMemberships) map { x =>
          cmd.succeeded(Some(mParams.groupId), Some(mParams.groupName))
      }
    }
    
    stage3 recover { 
        case cfe:CommandFailedException =>  cfe.command
        case ex:Throwable => cmd.failed("INT_ERR", ex.getMessage, None, None)
    } flatMap { c =>
      commandDb.upsertCommand(c)
    }  
  }

  private def combinePath(a:EffMembership, d:EffMembership) : String = {
    val path = 
      if (a==d) ""
      else if (a.memberId == d.groupId) a.path + " " + a.memberId + " " + d.path
      else a.path + " " + a.memberId + " " + d.groupId + " " + d.path
    path.trim()    
    
  }

}