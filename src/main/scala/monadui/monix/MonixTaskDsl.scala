package monadui.monix

import monix.eval.Task
import monix.execution.{Callback, Scheduler}

import language.experimental.macros
import scala.annotation.compileTimeOnly
import scala.reflect.macros.blackbox

/** A DSL for the `monix.Task` effect implement in terms of scalac's -Xasync phase. */
object MonixTaskDsl {
  def async[T](body: T): Task[T] = macro asyncImpl
  @compileTimeOnly("[async] `await` must be enclosed in `async`")
  def await[T](output: Task[T]): T = ???

  def asyncImpl(c: blackbox.Context)(body: c.Tree): c.Tree = {
    import c.universe._
    val awaitSym = typeOf[MonixTaskDsl.type].decl(TermName("await"))
    def mark(t: DefDef): Tree = c.internal.markForAsyncTransform(c.internal.enclosingOwner, t, awaitSym, Map.empty)
    val name = TypeName("stateMachine$async")
    q"""
      final class $name(scheduler: _root_.monix.execution.Scheduler, callback: _root_.monix.execution.Callback[_root_.scala.Throwable, _root_.scala.AnyRef]) extends _root_.monadui.monix.MonixTaskDsl.StateMachine(scheduler, callback) {
        ${mark(q"""override def apply(tr$$async: _root_.scala.Either[_root_.scala.Throwable, _root_.scala.AnyRef]): _root_.scala.Unit = ${body}""")}
      }
      _root_.monix.eval.Task.async0[_root_.scala.AnyRef](new $name(_, _)).asInstanceOf[${c.macroApplication.tpe}]
    """
  }

  abstract class StateMachine(scheduler: monix.execution.Scheduler, callback: Callback[Throwable, AnyRef]) extends monadui.AsyncStateMachine[Task[AnyRef], Either[Throwable, AnyRef]] with (Either[Throwable, AnyRef] => Unit) {
    var result$async: Task[AnyRef] = _

    // FSM translated method
    def apply(tr$async: Either[Throwable, AnyRef]): Unit

    // Required methods
    protected var state$async: Int = 0
    protected def completeFailure(t: Throwable): Unit = callback.onError(t)
    protected def completeSuccess(value: AnyRef): Unit = callback.onSuccess(value)
    protected def onComplete(f: Task[AnyRef]): Unit = {
      f.runAsync(this)(scheduler)
    }
    protected def getCompleted(f: Task[AnyRef]): Either[Throwable, AnyRef] = {
      // TODO get the immediate value out of a Now or Error.
      null
    }
    protected def tryGet(tr: Either[Throwable, AnyRef]): AnyRef = tr match {
      case Right(value) =>
        value.asInstanceOf[AnyRef]
      case Left(throwable) =>
        callback.onError(throwable)
        this // sentinel value to indicate the dispatch loop should exit.
    }

    apply(null)
  }
}
