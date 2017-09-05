package com.jrancier.tny.controllers

import play.api._
import play.api.libs.json.JsValue
import play.api.mvc._

object TnyController extends Controller {

  def getUrl = Action {

    Redirect("Jim")
  }

  def tnyifyUrl(json: JsValue) = Action { implicit request =>
    val name = (json \ "url").asOpt[String].get

    getNewId
    Ok("name : " + name)
  }

}