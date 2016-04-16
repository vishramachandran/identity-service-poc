package com.vishr.ids.cqrs.processor

import com.vishr.ids.db.model.Command

class CommandFailedException(val command:Command, nestedException: Throwable) extends Exception(command.errorMessage.getOrElse(""), nestedException) {
    def this(command:Command) = this(command, null)
}