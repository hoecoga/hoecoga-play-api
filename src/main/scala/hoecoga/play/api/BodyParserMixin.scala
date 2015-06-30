package hoecoga.play.api

import play.api.http.LazyHttpErrorHandler
import play.api.libs.iteratee._
import play.api.mvc._

import scala.concurrent.Future

trait BodyParserMixin extends Controller {
  private[this] val maxLength = parse.DefaultMaxTextLength

  /**
   * @see [[https://github.com/playframework/playframework/blob/2.4.x/framework/src/play/src/main/scala/play/api/mvc/ContentTypes.scala#L728]]
   */
  private[this] def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
    LazyHttpErrorHandler.onClientError(request, statusCode, msg)
  }

  /**
   * @see [[https://github.com/playframework/playframework/blob/2.4.x/framework/src/play/src/main/scala/play/api/mvc/ContentTypes.scala#L736]]
   */
  private[this] def checkForEof[A](request: RequestHeader): A => Iteratee[Array[Byte], Either[Result, A]] = { eofValue: A =>
    import Execution.Implicits.trampoline
    def cont: Iteratee[Array[Byte], Either[Result, A]] = Cont {
      case in @ Input.El(e) =>
        val badResult: Future[Result] = createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request)
        Iteratee.flatten(badResult.map(r => Done(Left(r), in)))
      case in @ Input.EOF =>
        Done(Right(eofValue), in)
      case Input.Empty =>
        cont
    }
    cont
  }

  /**
   * @example {{{
   *           Action.async(bytesParser) { request: Request[Array[Byte]] =>
   *             ???
   *           }
   * }}}
   */
  protected[this] val bytesParser: BodyParser[Array[Byte]] = BodyParser(s"bytes, maxLength=$maxLength") { request =>
    import Execution.Implicits.trampoline
    Traversable.takeUpTo[Array[Byte]](maxLength).transform(Iteratee.consume[Array[Byte]]()).flatMap(checkForEof(request))
  }
}
