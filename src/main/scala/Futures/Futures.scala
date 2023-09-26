package Futures

import java.lang.Thread.sleep
import java.util.concurrent.{ExecutorService, Executors, ForkJoinPool, TimeUnit}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

object RunningMultipleCalcs extends App {

  val initialParallelism = 1000

  val commonPool = ForkJoinPool.commonPool

//  val threadPool: ExecutorService = Executors.newFixedThreadPool(initialParallelism)
//  implicit val ex: ExecutionContext = ExecutionContext.fromExecutor(threadPool)

//  implicit val ex: ExecutionContext = ExecutionContext.fromExecutor(
//    Executors.newFixedThreadPool(initialParallelism)
//  )
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(initialParallelism)
  )

  time(multipleCalcs())
  object Cloud {
    def runAlgorithm(x: Integer): Future[Int] = {
      Future {
        sleep(500)
//        println(Thread.currentThread.getName)
        x
      }
    }
  }

  def multipleCalcs() = {
    println("starting futures")
    Future
      .sequence {
        (1 to 10000).toList
          .map(Cloud.runAlgorithm(_))
      }
      .onComplete { case Success(result) =>
        println(s"total = ${result.sum}")
      }
  }

  println("before sleep at the end")
  sleep(15000)
//  threadPool.shutdown()
//  threadPool.awaitTermination(10, TimeUnit.SECONDS) // Optionally wait for termination
}
object Futures1 extends App {
  val initialParallelism = 4
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(initialParallelism)
  )

  time(longComputation)

  def longRunningComputation(i: Int): Future[Int] = Future {
    sleep(100)
    i + 1
  }

  def longComputation() = {
    1 to 100 foreach { x =>
      longRunningComputation(x).onComplete {
        case Success(result) => println(s"result = $result")
        case Failure(e)      => e.printStackTrace
      }
    }
  }

  sleep(1000)
}
object Futures extends App {

  val initialParallelism = 4
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
