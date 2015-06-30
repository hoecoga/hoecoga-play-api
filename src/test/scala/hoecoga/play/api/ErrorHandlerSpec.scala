package hoecoga.play.api

import org.scalatest.FunSpec
import play.api.http.Writeable
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Action, BodyParsers}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}

class ErrorHandlerSpec extends FunSpec {
  import ErrorHandlerSpecHelper._

  describe("ErrorHandler") {
    it("onClientError") {
      test { settings =>
        import settings._

        def check[A](req: FakeRequest[A], code: Int)(implicit w: Writeable[A]): Unit = {
          val Some(res) = route(req)
          assert(status(res) === code)
          assert(contentAsJson(res) === Json.obj("meta" -> Json.obj("status" -> code)))
        }

        check(client.withTextBody(""), UNSUPPORTED_MEDIA_TYPE)
        check(FakeRequest(), NOT_FOUND)
        check(client.withJsonBody(Json.obj("a" -> "b" * BodyParsers.parse.DefaultMaxTextLength)), REQUEST_ENTITY_TOO_LARGE)
      }
    }
  }
}

object ErrorHandlerSpecHelper {
  val Method = POST
  val Path1 = "/client"

  case class Settings(client: FakeRequest[_])

  def test(f: Settings => Unit): Unit = {
    running(FakeApplication(
      withRoutes = {
        case (Method, Path1) => Action(BodyParsers.parse.json)(_ => Ok(""))
      },
      additionalConfiguration = Map("play.http.errorHandler" -> "hoecoga.play.api.ErrorHandler")
    )) {
      f(Settings(FakeRequest(Method, Path1)))
    }
  }
}
