package hoecoga.play.api

import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results._

trait ResultMixin {
  private[this] def metaJson(status: Int): JsObject = Json.obj("meta" -> Json.obj("status" -> status))

  private[this] def dataJson[A](data: A)(implicit w: Writes[A]) = Json.obj("data" -> data)

  protected[this] def result[A](status: Int): Result = Status(status)(metaJson(status))

  protected[this] def result[A](status: Int, data: A)(implicit w: Writes[A]): Result =
    Status(status)(metaJson(status) ++ dataJson(data))
}
