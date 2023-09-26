package Akka

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import Akka.GreeterMain.{SayHello, Stop}

object Greeter {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }
}

object GreeterBot {

  def apply(max: Int): Behavior[Greeter.Greeted] = {
    bot(0, max)
  }

  private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1
      context.log.info("Greeting {} for {}", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        message.from ! Greeter.Greet(message.whom, context.self)
        bot(n, max)
      }
    }
}

object GreeterMain {

  abstract trait GreeterCommand {
    def name: String
  }
  final case class SayHello(name: String) extends GreeterCommand
  case class Stop(name: String = "") extends GreeterCommand

  def apply(): Behavior[GreeterCommand] =
    Behaviors.setup { context =>
      val greeter = context.spawn(Greeter(), "greeter")

      Behaviors.receiveMessage {
        case message: SayHello =>
          val replyTo = context.spawn(GreeterBot(max = 3), message.name)
          greeter ! Greeter.Greet(message.name, replyTo)
          Behaviors.same

        case _: Stop =>
          Behaviors.stopped(() => println("Stopped"))
      }
    }
}

object AkkaIntro extends App {
  val greeterMain: ActorSystem[GreeterMain.GreeterCommand] =
    ActorSystem(GreeterMain(), "AkkaQuickStart")
  (1 to 100).toList.foldLeft(0) { (b, a) =>
    greeterMain ! SayHello(s"Charles${b}")
    b + a
  }
  greeterMain ! Stop()
}
