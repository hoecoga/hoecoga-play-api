package hoecoga.play.api

import java.util

import org.scalatest.FunSpec
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}

class BodyParserMixinSpec extends FunSpec {
  import BodyParserMixinSpecHelper._

  describe("BodyParser") {
    it("bytesParser#Ok") {
      test { settings =>
        import settings._
        Seq(
          ("abc".getBytes, "text/plain"),
          (Json.obj("a" -> "b").toString().getBytes, "text/json"),
          (<root><a>b</a></root>.toString().getBytes, "text/xml"),
          ((0 to 100).map(_.toByte).toArray, "application/octet-stream")).foreach {
          case (body, contentType) =>
            val Some(res) = route(req.withBody(body).withHeaders("Content-Type" -> contentType))
            assert(status(res) === OK)
            assert(contentAsJson(res) === Json.obj("meta" -> Json.obj("status" -> OK)))
            assert(util.Arrays.equals(body, app.bodies.head))
        }
      }
    }

    it("bytesParser#RequestEntityTooLarge") {
      test { settings =>
        import settings._
        val body = "a" * (app.parse.DefaultMaxTextLength + 1)
        val Some(res) = route(req.withBody(body.getBytes).withHeaders("Content-Type" -> "text/plain"))
        assert(status(res) === REQUEST_ENTITY_TOO_LARGE)
        assert(contentAsJson(res) === Json.obj("meta" -> Json.obj("status" -> REQUEST_ENTITY_TOO_LARGE)))
      }
    }
  }
}

object BodyParserMixinSpecHelper {
  val Method = POST
  val Path = "/bytes.json"

  class Application extends BodyParserMixin with ResultMixin {
    var bodies: List[Array[Byte]] = Nil

    def bytes: Action[Array[Byte]] = Action(bytesParser) { request =>
      bodies = request.body :: bodies
      result(OK)
    }
  }

  case class Settings(app: Application, req: FakeRequest[_])

  def test(f: Settings => Unit) = {
    val app = new Application
    running(FakeApplication(
      withRoutes = {
        case (Method, Path) => app.bytes
      },
      additionalConfiguration = Map("play.http.errorHandler" -> "hoecoga.play.api.ErrorHandler")
    )) {
      f(Settings(app, FakeRequest(Method, Path)))
    }
  }
}

