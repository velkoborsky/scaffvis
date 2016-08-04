package scaffvis

/**
  * The task runner - runs task specified as arguments one by one.
  */
object Application {

  def main(args: Array[String]): Unit = {

    if(args.isEmpty)
      println("Specify tasks to run.")

    for (task <- args) {
      try {
        val c = Class.forName(s"scaffvis.tasks.$task") //find the task using reflection
        println(s"Executing task: $task.")
        c.newInstance() //execute the task
      } catch {
        case e: ClassNotFoundException => {
          println(s"Task not found: $task.")
          System.exit(1)
        }
      }
    }

  }

}
