package scaffvis.processing

/**
  * Provides simplified means of measuring time during the processing tasks.
  */
object Stopwatch {

  private var lastTime: Long = 0

  def lap() = {
    val now = System.nanoTime()
    if(lastTime != 0) {
      val elapsedNano = now - lastTime
      val elapsed = (elapsedNano/1e6).toInt
      println(s"elapsed time: $elapsed ms")
    }
    lastTime = now
  }

  def apply(): Stopwatch = new Stopwatch()

}

case class Stopwatch(startTime: Long = System.currentTimeMillis()) {

  private var lastLap = startTime

  def lapTimeMs = {
    val currentTime = System.currentTimeMillis()
    val elapsed = currentTime - lastLap
    lastLap = currentTime
    elapsed
  }

  def totalTimeMs = {
    val currentTime = System.currentTimeMillis()
    currentTime - startTime
  }
}