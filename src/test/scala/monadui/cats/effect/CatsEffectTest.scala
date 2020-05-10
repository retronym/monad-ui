package monadui.cats.effect

import java.util.concurrent.atomic.AtomicInteger

import cats.effect._
import cats.implicits._

import scala.collection.mutable

object CatsEffectTest extends cats.effect.IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val x = new AtomicInteger()
    val t1 = IO(x.incrementAndGet())
    val t2 = IO.pure("10")

    def id[T](t: T) = IO.pure(t)

    val dsl = CatsEffectDsl[IO]
    import dsl._

    val task = async {
      var i = 0
      var r = mutable.ListBuffer[Int]()
      while (i < await(id(10))) {
        r += await(id(await(t1)))
        i += 1
      }
      (r.result(), await(t2))
    }
    task.map(r => {
      println(r); ExitCode.Success
    })
  }
}
