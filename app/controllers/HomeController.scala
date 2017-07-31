package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import akka.stream.Materializer

import akka.actor._

@Singleton
class HomeController @Inject()(cc: ControllerComponents)
  (implicit system: ActorSystem, mat: Materializer)
    extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def ws: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      RoomActor.props(out)
    }
  }
}

object RoomActor {
  def props(out: ActorRef) = Props(new RoomActor(out))
  val sampleRoom : JsValue = Json.obj(
    "type" -> "location",
    "name" -> "bob",
    "fullName" -> "sample scala room",
    "description" -> """So.. once there was a sample scala room.
                       |and this room was well described.
                       |in detail. So much detail. Bigly.""".stripMargin,
    "exits" -> Json.obj(
      "N" -> "North",
      "S" -> "South",
      "W" -> "West",
      "E" -> "East"
    ),
    "commands" -> Json.obj(
      "/play" -> "play with play"
    ),
    "roomInventory" -> List("yoyo","top")
  )

  def chat(message : String, username : String) : JsValue = Json.obj (
    "type" -> "chat",
    "username" -> username,
    "content" -> message,
    "bookmark" -> "GIRAFFE!"
  )
}

class RoomActor(out: ActorRef) extends Actor {
  val pattern =  """(?s)(\w+),([^,]*),(.*)""".r
  def receive =  {
    case str : String => str match {
      case "marco" => out ! ("polo")
      case pattern ("roomHello", id, payload) =>
        out ! ("player," + (Json.parse(payload) \ "userId").as[String] + "," + RoomActor.sampleRoom.toString)
      case pattern ("roomJoin", id, payload) =>
        out !  ("player," + (Json.parse(payload) \ "userId").as[String] + "," + RoomActor.sampleRoom.toString)
      case pattern ("roomGoodbye", id, payload) =>
        out ! ("Don't slam door on way out, please.")
      case pattern ("roomPart", id, payload)
          => out ! ("Don't slam door on way out, please.")
      case pattern ("room", id, payload) if ((Json.parse(payload) \ "content").as[String]).startsWith("/")  =>
        out ! ("Your wish is my command.")
      case pattern ("room", id, payload)   =>
        out ! ("player,*," + RoomActor.chat( (Json.parse(payload) \ "content").as[String],  (Json.parse(payload) \ "username").as[String]))
      case _ => out ! ("whatever, I dont care...") 
    }
    case _ => out ! ("So long, and thanks for the fish.")
  }

  override def preStart = out ! ("""ack,{
                                   |"version": [1,2]
                                   |}""".stripMargin
  )
}
