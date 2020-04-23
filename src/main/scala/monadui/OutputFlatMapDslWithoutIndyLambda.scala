package monadui

object OutputFlatMapDslWithoutIndyLambda {
  implicit class RichOutput[T](val output: Output[T]) extends AnyVal {
    def flatMap[U](f: CustomFunction[T, Output[U]]): Output[U] = {
      output.value match {
        case Some(v) => f(v) match {
          case Output(s @ Some(_), written1) => Output(s, Output.mergeMultiMap(output.written, written1))
          case Output(None, written1) => Output(None, Output.mergeMultiMap(output.written, written1))
        }
        case None => Output(None, output.written)
      }
    }
    def map[U](f: CustomFunction[T, U]): Output[U] = {
      Output(output.value.map(f), output.written)
    }
    def filter(f: CustomFunction[T, Boolean]): Output[T] = {
      if (output.value.exists(f)) output
      else Output(None, output.written)
    }
    def withFilter(f: CustomFunction[T, Boolean]): Output[T] = filter(f)
  }
  // scalac will generate anonymous inner classes for these.
  abstract class CustomFunction[-A, +B] extends (A => B)
}

