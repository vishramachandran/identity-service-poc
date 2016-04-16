package com.vishr.ids.common

import com.vishr.ids.api.model.v1._
import java.util.UUID
import net.liftweb.json._
import net.liftweb.json.Serialization._
import net.liftweb.json.JsonDSL._
import com.github.vonnagy.service.container.http.DefaultMarshallers
import net.liftweb.json.ext.JodaTimeSerializers
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import java.util.TimeZone



object LiftFormats {
  
  implicit val formats = DefaultFormats.withHints(ShortTypeHints(List(
        classOf[UserCreateCommandParams], 
        classOf[UserUpdateCommandParams],
        classOf[UserDeleteCommandParams], 
        classOf[GroupCreateCommandParams],
        classOf[GroupUpdateCommandParams],
        classOf[GroupDeleteCommandParams], 
        classOf[MembershipCreateParams],
        classOf[MembershipDeleteParams],
        classOf[Command],
        classOf[User],
        classOf[Membership],
        classOf[ApiError],
        classOf[Group]))) + UUIDSerializer + DateTimeSerializer
}

case object UUIDSerializer extends CustomSerializer[UUID](format => ( {
  case JString(u) => UUID.fromString(u)
  case JNull => null
  case u => UUID.fromString(u.toString)
}, {
  case u: UUID => JString(u.toString)
}))

case object DateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => 
      val fmt = ISODateTimeFormat.dateTime();
      fmt.parseDateTime(s)
      
      
    case JNull => null
  },
  {
    case d: DateTime => 
      val fmt = ISODateTimeFormat.dateTime();
      JString(fmt.print(d))
  }
))

