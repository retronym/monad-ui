package monadui

import scala.collection.immutable.HashMap

object OutputFlatMapDsl {
  implicit class RichOutput[T](val output: Output[T]) extends AnyVal {
    def flatMap[U](f: T => Output[U]): Output[U] = {
      output.value match {
        case Some(v) => f(v) match {
          case Output(s @ Some(_), written1) => output.withValue(s, written1)
          case Output(None, written1) => output.withValue(None, written1)
        }
        case None => this.asInstanceOf[Output[U]]
      }
    }
    def map[U](f: T => U): Output[U] = {
      if (output.value.isDefined)
        output.withValue(Some(f(output.value.get)), HashMap.empty)
      else this.asInstanceOf[Output[U]]
    }
    def filter(f: T => Boolean): Output[T] = {
      if (output.value.exists(f)) output
      else output.withValue(None, HashMap.empty)
    }
    def withFilter(f: T => Boolean): Output[T] = filter(f)
  }
}
