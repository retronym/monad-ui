package monadui.cats.effect

import cats.Monad
import cats.effect.{ConcurrentEffect, Effect, IO}

import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.macros.blackbox

/** A DSL for the `cats.Monad` effect implemented in terms of scalac's -Xasync phase.
 *  WARNING: only safe for single-shot effects! */
class CatsMonadDsl[M[_]] {
  def async[T](body: T)(implicit M: Monad[M]): M[T] = macro CatsMonadDsl.asyncImpl
  @compileTimeOnly("[async] `await` must be enclosed in `async`")
  def await[T](output: M[T]): T = ???
}

object CatsMonadDsl {
  def apply[M[_]]: CatsMonadDsl[M] = new CatsMonadDsl[M]
  def asyncImpl(c: blackbox.Context)(body: c.Tree)(M: c.Tree): c.Tree = {
    import c.universe._
    val awaitSym = typeOf[CatsMonadDsl[Any]].decl(TermName("await"))
    def mark(t: DefDef): Tree = c.internal.markForAsyncTransform(c.internal.enclosingOwner, t, awaitSym, Map.empty)
    val name = TypeName("stateMachine$async")
    q"""
      final class $name extends ${symbolOf[StateMachine[_]]}($M) {
        ${mark(q"""override def apply(tr$$async: ${typeOf[AnyRef]}): ${typeOf[Unit]} = ${body}""")}
      }
      val f = new $name
      f.result.asInstanceOf[${c.macroApplication.tpe}]
    """
  }

  abstract class StateMachine[M[_]](M: Monad[M]) extends monadui.AsyncStateMachine[M[AnyRef], AnyRef] with (AnyRef => Unit) {
    // FSM translated method
    def apply(tr$async: AnyRef): Unit
    var result: M[AnyRef] = M.pure(null)

    // Required methods
    protected var state$async: Int = 0
    protected def completeFailure(t: Throwable): Unit = throw t
    protected def completeSuccess(value: AnyRef): Unit = result = M.pure(value)
    protected def onComplete(f: M[AnyRef]): Unit = {
      result = M.flatMap(f){v => this(v); result}
    }
    protected def getCompleted(f: M[AnyRef]): AnyRef = null
    protected def tryGet(tr: AnyRef): AnyRef = tr

    apply(null)
  }
}
