package monadui

import scala.collection.immutable.HashMap

/** The same API as OutputFlatMapDsl, but uses a custom function type that can't be compiled to invokedynamic
 *  LambdaMetafactory. This increases the constant factor of the code size overhead of flatMap chaining as each
 *  lambda is compiled to a .class file, rather a method in the enclosing class.
 */
object OutputFlatMapDslWithoutIndyLambda {
  implicit class RichOutput[T](val output: Output[T]) extends AnyVal {
    def flatMap[U](f: CustomFunction[T, Output[U]]): Output[U] = {
      output.value match {
        case Some(v) => f(v) match {
          case Output(s @ Some(_), written1) => output.withValue(s, written1)
          case Output(None, written1) => output.withValue(None, written1)
        }
        case None => this.asInstanceOf[Output[U]]
      }
    }
    def map[U](f: CustomFunction[T, U]): Output[U] = {
      if (output.value.isDefined)
        output.withValue(output.value.map(f), HashMap.empty)
      else this.asInstanceOf[Output[U]]
    }
    def filter(f: CustomFunction[T, Boolean]): Output[T] = {
      if (output.value.exists(f)) output
      else output.withValue(None, HashMap.empty)
    }
    def withFilter(f: CustomFunction[T, Boolean]): Output[T] = filter(f)
  }
  // scalac will generate anonymous inner classes for these.
  abstract class CustomFunction[-A, +B] extends (A => B)
}

