package com.vishr.ids.cqrs.processor.model

import java.util.UUID

case class UserAttributesChanged(tenantId:UUID, userId:UUID)
case class GroupAttributesChanged(tenantId:UUID, userId:UUID)