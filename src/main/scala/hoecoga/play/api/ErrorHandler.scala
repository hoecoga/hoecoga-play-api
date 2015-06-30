package hoecoga.play.api

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.http.Status._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

/**
 * @see [[https://www.playframework.com/documentation/2.4.x/ScalaErrorHandling]]
 */
class ErrorHandler extends HttpErrorHandler with ResultMixin {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger.debug(s"onClientError(request = $request, statusCode = $statusCode, message = $message)")
    Future.successful(result(statusCode))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger.error(s"onServerError(request = $request, exception = $exception)", exception)
    Future.successful(result(INTERNAL_SERVER_ERROR))
  }
}
