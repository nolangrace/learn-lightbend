package com.example

import com.example.actor.SessionActor.{ResponsePackage}

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val usersJsonFormat = jsonFormat1(ResponsePackage)
}
//#json-formats
