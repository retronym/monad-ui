package monadui.monix

import java.util.concurrent.atomic.AtomicInteger

import MonixTaskDsl._
import monix.eval.Task

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MonixTest {
  def main(args: Array[String]): Unit = {
    val x = new AtomicInteger()
    val t1 = Task.eval(x.incrementAndGet())
    val t2 = Task("10")
    def id[T](t: T) = Task(t)
    val task = async {
      var i = 0
      var r = mutable.ListBuffer[Int]()
      while (i < await(id(10))) {
        r += await(id(await(t1)))
        i += 1
      }
      (r.result(), await(t2))
    }
    import monix.execution.Scheduler.Implicits.global
    println(Await.result(task.runToFuture, Duration.Inf))
  }
}
