package com.vishr.ids.cqrs.query

import com.github.vonnagy.service.container.ContainerBuilder
import com.vishr.ids.api._
import com.vishr.ids.api._
import com.vishr.ids.common.DependencyInjector
import com.vishr.ids.api.CommandApi
import com.vishr.ids.api.GroupApi
import com.vishr.ids.api.UserApi

object ContainerMain extends App {

  val startDi = DependencyInjector.cassandraProvider
  
  // Here we establish the container and build it while
  // applying extras.
  val container = new ContainerBuilder()
    // Register the API routes
    .withRoutes(classOf[UserApi])
    .withRoutes(classOf[GroupApi])
    .withRoutes(classOf[CommandApi])
    .build

  container.start
  
}