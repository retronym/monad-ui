package monadui

import monadui.Output.mergeMultiMap

import scala.util.control.ControlThrowable

/** A lambda-free DSL that records all encounter 'written' values in a scope object as they
 *  are extracted, and appends all these into a the result of the `writing` block.
 *
 *  Optionality is implemented with as an exception.
 */
object OutputImplicitDsl {

  final class OutputScope {
    private var written = collection.immutable.HashMap[String, Vector[Any]]()
    def append(written: collection.immutable.HashMap[String, Vector[Any]]): Unit = {
      this.written = mergeMultiMap(this.written, written)
    }
    val noSuchElementException: NoSuchElementException = new NoSuchElementException with ControlThrowable
    def result[T](value: Option[T]): Output[T] = Output(value, written)
  }

  def writing[T](f: OutputScope => T): Output[T] = {
    val scope = new OutputScope
    try {
      scope.result(Some(f(scope)))
    } catch {
      case nsee: NoSuchElementException if nsee eq scope.noSuchElementException =>
        scope.result(None)
    }
  }
}
