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

  def room: WebSocket = WebSocket.accept[String, String] { request =>
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
    "bookmark" -> "!*?#"
  )

  def usernameOf (payload :String) = {
    (Json.parse(payload) \ "username").as[String]
  }

  def contentOf (payload :String) = {
    (Json.parse(payload) \ "content").as[String]
  }

   def userIdOf (payload :String) = {
    (Json.parse(payload) \ "userId").as[String]
   }

  def !? ( payload : String, command : String = "" ) : Boolean = {
    contentOf(payload).startsWith("/" + command)
  }

  def isDirection (payload : String) = contentOf(payload) match {
    case x if x.length >= 5 => { 
      contentOf(payload).substring(4,5).toUpperCase match {
        case "N" | "S" | "W" | "E" => true
        case _                     => false
      }
    }
    case _                   => false
  }

  def command (payload: String) : String = payload match {
    case x if !? (x, "go") && isDirection(x)  => "playLocation," + userIdOf(payload) + "," +  move(contentOf(x).substring(4))
    case x if !? (x, "play") => "player,*," + play(usernameOf(payload), userIdOf(payload))
    case _                   => "player,*," + unimplemented(usernameOf(payload), userIdOf(payload))
  }

  def play(username : String, userid : String) : JsValue = Json.obj (
    "type" -> "event",
    "content" -> Json.obj (
      "*" -> (username + " plays"),
      userid -> "you play"
    ),
    "bookmark" -> "!*?#"
  )

   def unimplemented(username : String, userid : String) : JsValue = Json.obj (
    "type" -> "event",
    "content" -> Json.obj (
      "*" -> (username + " attempts an unimplemented command"),
      userid -> "you attempt an unimplemented command"
    ),
    "bookmark" -> "!*?#"
  )

  def move(direction : String) : JsValue = direction.substring(0, 1).toUpperCase match {
    case "N" | "S" | "W" | "E" => Json.obj (
      "type" -> "exit",
      "content" -> "You exit through the door",
      "exitId" -> direction.substring(0, 1).toUpperCase
    )
    case _ => ???
  }
}

class RoomActor(out: ActorRef) extends Actor {
  import RoomActor._
  val p =  """(?s)(\w+),([^,]*),(.*)""".r
  def receive =  {
    case str : String => str match {
      case "willy"                   => out ! ("nilly")
      case p ("roomHello", _, x)     => out ! ("player," + userIdOf(x)  + "," + sampleRoom)
      case p ("roomJoin", _, x)      => out ! ("player," + userIdOf(x) + "," + sampleRoom)
      case p ("roomGoodbye", _, _)   => out ! ("Don't slam door on way out, please.")
      case p ("roomPart", _, _)      => out ! ("Don't slam door on way out, please.")
      case p ("room", _, x) if !?(x) => out ! (command(x))
      case p ("room", _, x)          => out ! ("player,*," + chat( contentOf(x),  usernameOf(x)))
      case _                         => out ! ("whatever, I dont care...") 
    }
    case _            => out ! ("So long, and thanks for the fish.")
  }

  override def preStart = out ! ("""ack,{
                                   |"version": [1,2]
                                   |}""".stripMargin
  )
}
