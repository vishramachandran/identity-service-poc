package com.vishr.ids.cqrs.processor

import com.vishr.ids.api.model.v1.GroupCreateCommandParams
import com.vishr.ids.api.model.v1.GroupDeleteCommandParams
import com.vishr.ids.api.model.v1.GroupUpdateCommandParams
import com.vishr.ids.db.model.Command
import com.vishr.ids.common.DependencyInjector
import com.datastax.driver.core.utils.UUIDs
import com.vishr.ids.db.model.Group
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.vishr.ids.api.model.v1.CommandParams
import com.vishr.ids.api.model.v1.DynamicMembershipUpdateParams
import com.vishr.ids.cqrs.processor.model.GroupAttributesChanged
import com.vishr.ids.cqrs.processor.model.UserAttributesChanged

object DynamicMembershipCommandProcessor {
  
  def groupDb = DependencyInjector.groupDb
  def commandDb = DependencyInjector.commandDb
  def ancestryDb = DependencyInjector.ancestryDb

  def userAttributesChanged(cmd: Command, params: UserAttributesChanged) = {
    // list current direct memberships for user
    // list all groups in tenant
    //    if user satisfies membership condition and membership status has changed, then issue a MembershipCreateParams or MembershipDeleteParams
  }

  def groupAttributesChanged(cmd: Command, params: GroupAttributesChanged) = {
    // list current direct memberships for group
    // list all groups in tenant
    //    if group satisfies membership condition and membership status has changed, then issue a MembershipCreateParams or MembershipDeleteParams
    
  }

  def groupMembershipChanged(cmd: Command, params: DynamicMembershipUpdateParams) = {
    // list current direct members for group
    // list all groups in tenant
    //    if group satisfies membership condition and membership status has changed, then issue a MembershipCreateParams or MembershipDeleteParams
    // list all users in tenant
    //    if user satisfies membership condition and membership status has changed, then issue a MembershipCreateParams or MembershipDeleteParams
    
  }

  

}