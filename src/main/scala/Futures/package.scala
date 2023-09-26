package object Futures {

  def time[B](block: => B): Unit = {
    val t0 = System.nanoTime()
    block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / 1000000 + "ms")
  }

}
