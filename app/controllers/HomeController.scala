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

  def getUserName (payload :String) = {
    (Json.parse(payload) \ "username")
  }

   def getUserId (payload :String) = {
    (Json.parse(payload) \ "userId")
  }

  def command (payload: String, command : String) : String = command match {
    case x if (x.startsWith("/go ")) => "playLocation," + getUserId(payload).as[String] + "," +  move(command.substring(4)).toString
    case x if (x.startsWith("/play ")) => "player,*," + play(getUserName(payload).as[String],getUserId(payload).as[String])
    case _ => "player,*," + unimplemented(getUserName(payload).as[String],getUserId(payload).as[String])
  }

  def play(username : String, userid : String) : JsValue = Json.obj (
    "type" -> "event",
    "content" -> Json.obj (
      "*" -> (username + " plays"),
      userid -> "you play"
    ),
    "bookmark" -> "ELEPHANT!"
  )

   def unimplemented(username : String, userid : String) : JsValue = Json.obj (
    "type" -> "event",
    "content" -> Json.obj (
      "*" -> (username + " attempts an unimplemented command"),
      userid -> "you attempt an unimplemented command"
    ),
    "bookmark" -> "PENGUIN!"


  )

  def move(direction : String) : JsValue = direction.substring(0, 1) match {
    case "N" | "S" | "W" | "E" => Json.obj (
      "type" -> "exit",
      "content" -> "You exit through the door",
      "exitId" -> direction.substring(0, 1).toUpperCase
    )
    case _ => Json.obj() // Do something more intelligent here.
  }
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
      case pattern ("room", id, payload) if ((Json.parse(payload) \ "content").as[String]).startsWith("/")
          => out ! (RoomActor.command( payload, (Json.parse(payload) \ "content").as[String]) )
      case pattern ("room", id, payload)
          => out ! ("player,*," + RoomActor.chat( (Json.parse(payload) \ "content").as[String],  (Json.parse(payload) \ "username").as[String]))
      case _ => out ! ("whatever, I dont care...") 
    }
    case _ => out ! ("So long, and thanks for the fish.")
  }

  override def preStart = out ! ("""ack,{
                                   |"version": [1,2]
                                   |}""".stripMargin
  )
}
