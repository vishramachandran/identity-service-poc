package com.vishr.ids.common

import com.vishr.ids.cqrs.query.UserService
import com.vishr.ids.cqrs.query.UserServiceImpl
import com.vishr.ids.cqrs.query.GroupService
import com.vishr.ids.cqrs.command.CommandService
import com.vishr.ids.cqrs.command.CommandServiceImpl
import com.vishr.ids.cqrs.query.GroupServiceImpl
import com.vishr.ids.db.CommandDb
import com.vishr.ids.db.UserDb
import com.vishr.ids.db.CommandDbImpl
import com.vishr.ids.db.GroupDbImpl
import com.vishr.ids.db.UserDbImpl
import com.vishr.ids.db.GroupDb
import com.vishr.ids.cqrs.command.LocalKafkaCommandDispatcher
import com.vishr.ids.db.LocalCassandraProvider
import com.vishr.ids.cqrs.processor.CommandProcessorImpl
import com.vishr.ids.cqrs.command.CommandDispatcher
import com.vishr.ids.db.CassandraProvider
import com.vishr.ids.cqrs.processor.CommandProcessor
import com.vishr.ids.db.AncestryDbImpl
import com.vishr.ids.db.AncestryDb

object DependencyInjector {

  // TODO fix this cheap dependency injector
  // Order matters - dont remove cass from top 
  val cassandraProvider:CassandraProvider = LocalCassandraProvider
  val messagingDispatcher:CommandDispatcher = LocalKafkaCommandDispatcher

  val userService:UserService = UserServiceImpl
  val groupService:GroupService = GroupServiceImpl
  val commandService:CommandService = CommandServiceImpl
  
  val userDb:UserDb = UserDbImpl
  val groupDb:GroupDb = GroupDbImpl
  val commandDb:CommandDb = CommandDbImpl
  val ancestryDb:AncestryDb = AncestryDbImpl
  
  val commandProcessor:CommandProcessor = CommandProcessorImpl
}