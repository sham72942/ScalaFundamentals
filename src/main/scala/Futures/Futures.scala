package Futures


// 1 - the imports
import java.lang.Thread.sleep
import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object Futures extends App {

  val initialParallelism = 20
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(initialParallelism)
  )
  blockingFutures

  def two = Future {
    sleep(500)
    1 + 1
  }

  def blockingFutures() = {
    1 to 10 foreach { _ =>
      val result = Await.result(two, Duration("1 second"))
      println(result)
    }
  }

  sleep(1000)

}
