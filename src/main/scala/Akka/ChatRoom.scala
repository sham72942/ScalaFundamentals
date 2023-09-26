package Akka

import Akka.ChatRoom.GetSession
import akka.NotUsed
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Main extends App {
  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val charRoomRef = context.spawn(ChatRoom(), "chatRoom")
      val gabblerRef = context.spawn(Gabbler(), "gabbler")

      context.watch(gabblerRef)

      charRoomRef ! GetSession("shamoel", gabblerRef)

      Behaviors.receiveSignal { case (_, Terminated(_)) =>
        context.log.info(s"Terminate called for $context")
        Behaviors.stopped
      }
    }
  }

  ActorSystem(apply(), "ChatRoomDemo")
}
object ChatRoom {
  sealed trait RoomCommand
  final case class GetSession(
    screenName: String,
    replyTo: ActorRef[SessionEvent]
  ) extends RoomCommand
  private final case class PublishSessionMessage(
    screenName: String,
    message: String
  ) extends RoomCommand

  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage])
    extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String)
    extends SessionEvent

  sealed trait SessionCommand
  final case class PostMessage(message: String) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted)
    extends SessionCommand
  def apply(): Behavior[RoomCommand] =
    chatRoom(List.empty)

  private def chatRoom(
    sessions: List[ActorRef[SessionCommand]]
  ): Behavior[RoomCommand] = {
    Behaviors.receive { (context, message) =>
      message match {
        case GetSession(screenName, replyTo) =>
          context.log.info(s"GetSession called for $screenName")
          val childSession = context.spawn(
            session(context.self, screenName, replyTo),
            name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name())
          )
          replyTo ! SessionGranted(childSession)
          chatRoom(childSession :: sessions)

        case PublishSessionMessage(screenName: String, message: String) =>
          context.log.info(
            s"PublishSessionMessage called for $screenName, with message: $message"
          )
          val notification = NotifyClient(MessagePosted(screenName, message))
          sessions.foreach(_ ! notification)
          Behaviors.same

      }
    }
  }

  private def session(
    room: ActorRef[PublishSessionMessage],
    screenName: String,
    client: ActorRef[SessionEvent]
  ): Behavior[SessionCommand] =
    Behaviors.receiveMessage {
      case PostMessage(message) =>
        room ! PublishSessionMessage(screenName, message)
        Behaviors.same
      case NotifyClient(message) =>
        client ! message
        Behaviors.same
    }
}

object Gabbler {
  import ChatRoom._
  def apply(): Behavior[SessionEvent] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case SessionGranted(handle) =>
          context.log.info(s"SessionGranted called for $handle")
          handle ! PostMessage("Hi, This is Shamoel Ahmad")
          Behaviors.same
        case MessagePosted(screenName, message) =>
          context.log.info(
            s"MessagePosted called for $screenName, with message $message"
          )
          context.log.info2(
            "message has been posted by '{}': {}",
            screenName,
            message
          )
          Behaviors.stopped
      }
    }
}
