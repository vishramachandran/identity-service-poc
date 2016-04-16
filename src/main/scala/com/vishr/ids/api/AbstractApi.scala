package com.vishr.ids.api

import akka.actor.{ ActorRefFactory, ActorSystem }
import com.github.vonnagy.service.container.http.routing.RoutedEndpoints
import scala.util.Failure
import scala.util.Success
import spray.http.StatusCodes._
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing.RequestContext
import scala.concurrent.Future
import spray.httpx.marshalling.ToResponseMarshaller
import com.vishr.ids.api.model.v1._
import com.vishr.ids.common.LiftFormats
import spray.http.StatusCode

abstract class AbstractApi(implicit system: ActorSystem,
    actorRefFactory: ActorRefFactory) extends RoutedEndpoints {

  // Import the default Json marshaller and un-marshaller
  implicit val marshaller = jsonMarshaller
  override def jsonFormats = LiftFormats.formats

  def complete[T](c: RequestContext, f: Future[T], statusCode: StatusCode = OK)(implicit m: ToResponseMarshaller[T]) = f onComplete {
    case Success(value) =>
      value match {
        case None => c.complete(NotFound, ApiError("404", "NOT-FOUND", "Not Found", None))
        case _ => c.complete(value) // TODO fix and respond with status code passed as arg
      }

    case Failure(ex) => c.complete(InternalServerError, ApiError("500", "INT-ERR", ex.getMessage, None))
  }

  def completeBadRequest(c: RequestContext, a:ApiError) = {
        c.complete(BadRequest, a)
  }
  
  
}