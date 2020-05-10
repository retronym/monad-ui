package monadui.cats.effect

import java.util.concurrent.atomic.AtomicInteger

import cats.effect._
import cats.implicits._

import scala.collection.mutable

object CatsMonadTest extends cats.effect.IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val x = new AtomicInteger()
    val t1 = IO(x.incrementAndGet())
    val t2 = IO.pure("10")

    def id[T](t: T) = IO.pure(t)

    val dsl = CatsMonadDsl[IO]
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

    // List is not a single-shot effect so the results are undefined!
    {
      val listDsl = CatsMonadDsl[List]
      val result: List[Int] = listDsl.async {
        listDsl.await(List(1, 2)) + listDsl.await(List(3, 4))
      }
      assert(result == List(4, 5, 3))

      val correct = for (i <- List(1, 2); j <- List(3, 4)) yield i + j
      assert(correct == List(4, 5, 5, 6))
    }

    {
      val optionDsl = CatsMonadDsl[Option]
      val result1: Option[Int] = optionDsl.async {
        optionDsl.await(Some(1)) + optionDsl.await(Some(2))
      }
      assert(result1 == Some(3))

      val result2: Option[Int] = optionDsl.async {
        optionDsl.await(None : Option[Int]) + optionDsl.await(Some(2))
      }
      assert(result2 == None)
    }

    task.map(r => {
      println(r); ExitCode.Success
    })
  }
}
