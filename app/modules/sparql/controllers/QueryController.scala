package modules.sparql.controllers

import javax.inject.{Inject, Singleton}
import modules.security.services.ProfileService
import modules.sparql.models.QueryCreate
import modules.sparql.services.QueryService
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

@Singleton
class QueryController @Inject()(profileService: ProfileService,
                                queryService: QueryService,
                                cc: ControllerComponents)
                               (implicit ec: ExecutionContext) extends AbstractController(cc) {
  def create(): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>
    val profile = profileService.getProfile(request).get
    val queryCreate = request.body.as[QueryCreate]
    queryService.create(queryCreate, profile.getUsername) map { query =>
      Ok(Json.toJson(query))
    }
  }
}
