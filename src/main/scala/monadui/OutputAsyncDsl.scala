package monadui

import monadui.OutputImplicitDsl.OutputScope

import language.experimental.macros
import scala.annotation.compileTimeOnly
import scala.reflect.macros.blackbox

/** A DSL for the `Output` effect implement in terms of scalac's -Xasync phase. */
object OutputAsyncDsl {
  def writing[T](body: T): Output[T] = macro writingImpl
  @compileTimeOnly("[async] `value` must be enclosed in `writing`")
  def value[T](output: Output[T]): T = ???

  def writingImpl(c: blackbox.Context)(body: c.Tree): c.Tree = {
    import c.universe._
    val awaitSym = typeOf[OutputAsyncDsl.type].decl(TermName("value"))
    def mark(t: DefDef): Tree = c.internal.markForAsyncTransform(c.internal.enclosingOwner, t, awaitSym, Map.empty)
    val name = TypeName("stateMachine$async")
    q"""
      final class $name extends _root_.monadui.OutputAsyncDsl.StateMachine {
        ${mark(q"""override def apply(tr$$async: _root_.scala.Option[_root_.scala.AnyRef]) = ${body}""")}
      }
      new $name().start().asInstanceOf[${c.macroApplication.tpe}]
    """
  }

  abstract class StateMachine extends AsyncStateMachine[Output[AnyRef], Option[AnyRef]] {
    val scope = new OutputScope
    var result$async: Output[AnyRef] = _

    // FSM translated method
    def apply(tr$async: Option[AnyRef]): Unit

    // Required methods
    protected var state$async: Int = 0
    protected def completeFailure(t: Throwable): Unit = throw t
    protected def completeSuccess(value: AnyRef): Unit = result$async = scope.result(Some(value))
    protected def onComplete(f: Output[AnyRef]): Unit = ???
    protected def getCompleted(f: Output[AnyRef]): Option[AnyRef] = {
      scope.append(f.written)
      f.value
    }
    protected def tryGet(tr: Option[AnyRef]): AnyRef = tr match {
      case Some(value) =>
        value.asInstanceOf[AnyRef]
      case None =>
        result$async = scope.result(None)
        this // sentinel value to indicate the dispatch loop should exit.
    }
    def start(): Output[AnyRef] = {
      apply(None)
      result$async
    }
  }
}
