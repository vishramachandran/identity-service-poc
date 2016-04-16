package com.vishr.ids.common

import java.util.UUID

object Constants {
  val zero = UUID.fromString("0000000-0000-0000-0000-000000000000")  
  val UuidRegex = "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})"
  val NameOrUuidRegex = "([a-zA-Z0-9_\\-@\\.]{1,50})".r
  

}