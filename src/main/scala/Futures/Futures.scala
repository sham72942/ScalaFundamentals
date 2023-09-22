package Futures

// 1 - the imports
import java.lang.Thread.sleep
import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

object Futures extends App {

  val initialParallelism = 20
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(initialParallelism)
  )
  println("timing nonBlockingFutures")
  time(nonBlockingFutures())
  println("timing blockingFutures")
  time(blockingFutures())
  println("timing usingOnComplete")
  time(usingOnComplete())

  def two = Future {
    sleep(500)
    1 + 1
  }

  def blockingFutures(): Unit = {
    1 to 10 foreach { _ =>
      println(Await.result(two, Duration("1 second")))
    }
  }

  def nonBlockingFutures(): Unit = {
    1 to 10 foreach { _ =>
      two.map(println)
    }
  }

  def usingOnComplete(): Unit = {
    println("starting calculation ...")
    val f = Future {
      val rand = Random.nextInt(500)
      sleep(rand)
      rand
    }
    println("before onComplete")
    1 to 10 foreach { _ =>
      f.onComplete {
        case Success(value) => print(s"Got the callback, meaning = $value;\t")
        case Failure(e)     => e.printStackTrace()
      }
    }
  }

  sleep(1000)

}
