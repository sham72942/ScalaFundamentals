package Akka

import Akka.Greeter.{Greet, Greeted}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class AkkaQuickstartSpec
  extends ScalaTestWithActorTestKit
  with AnyWordSpecLike
  with BeforeAndAfterAll {
  override def afterAll(): Unit = super.afterAll()

  ConfigFactory
    .parseString("""
    akka.loglevel = DEBUG
    akka.log-config-on-start = on
    """)
    .withFallback(ConfigFactory.load())

  "A Greeter" must {
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())

      underTest ! Greet("Shamoel", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Shamoel", underTest.ref))
    }
  }

}
