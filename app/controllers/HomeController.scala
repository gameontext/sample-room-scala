package controllers

import javax.inject._
import play.api._
import play.api.mvc._
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
}

class RoomActor(out: ActorRef) extends Actor {

  def receive = {
    case "marco" => out ! ("polo")
    case msg: String =>
      out ! ("I received your message: " + msg)
  }

  override def preStart = out ! ("""ack,{
                                   |"version": [1,2]
                                   |}""".stripMargin
  )
}
