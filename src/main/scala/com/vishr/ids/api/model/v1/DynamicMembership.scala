package com.vishr.ids.api.model.v1

import com.vishr.ids.common.Constants._
import java.util.UUID

case class DynamicMembershipUpdateParams(
    groupIdOrName: String,
    rule: Rule) extends CommandParams {
  override val entityId = if (groupIdOrName.matches(UuidRegex)) Some(UUID.fromString(groupIdOrName)) else None
  override val entityName = if (!groupIdOrName.matches(UuidRegex)) Some(groupIdOrName) else None
}


trait Vote
case object Yes extends Vote
case object No extends Vote
case object Refrain extends Vote
case class Invalid(why:String) extends Vote

trait Rule {
  def isSatisfiedBy(g:Group):Vote
  def isSatisfiedBy(g:User):Vote
}

// attribute operator value  ===> example:  lastname startsWith b
case class SimpleRule(att:String, op:String, vaal:String) extends Rule {
  def isSatisfiedBy(g:Group):Vote = {
    (att, op, vaal) match {
      case ("group", "is", g1) => if (g1 == g.groupName || g1 == g.groupId.toString) Yes else Refrain
      case ("group", "isNot", g1) => if (g1 == g.groupName || g1 == g.groupId.toString) No else Refrain
      case _ => Refrain
    }
  }
  def isSatisfiedBy(u:User):Vote = {
    (att, op, vaal) match {
      case ("username", "is", un) => if (un == u.username) Yes else Refrain
      case ("username", "isNot", un) => if (un == u.username) No else Refrain
      case ("lastName", "is", ln) => if (ln == u.lastName) Yes else Refrain
      case _ => Refrain
    }
  }
}

case class CompositeRule(c1:Rule, boolOp:String, c2:Rule) extends Rule {
  def isSatisfiedBy(g:Group):Vote = {
    val v1 = c1.isSatisfiedBy(g)
    (v1, boolOp) match {
      case (Yes, "or") =>  Yes     // TODO should we confirm that v2 is also No
      case (Refrain, "or") =>  c2.isSatisfiedBy(g)
      case (No, "or") =>  No
      case (Yes, "and") =>  c2.isSatisfiedBy(g)
      case (Refrain, "and") => Refrain 
      case (No, "and") =>  No
      case (Invalid(_), _) => v1 
      case _ => Invalid("Invalid operator: " + boolOp)
    }
  }
  def isSatisfiedBy(u:User):Vote = {
    val v1 = c1.isSatisfiedBy(u)
    (v1, boolOp) match {
      case (Yes, "or") =>  Yes
      case (Refrain, "or") =>  c2.isSatisfiedBy(u)
      case (No, "or") =>  No
      case (Yes, "and") =>  c2.isSatisfiedBy(u)
      case (Refrain, "and") => Refrain 
      case (No, "and") =>  No
      case (Invalid(_), _) => v1 
      case _ => Invalid("Invalid operator: " + boolOp)
    }
  }
  
}




