package monadui.cats.effect

import cats.effect.{Effect, IO}

import language.experimental.macros
import scala.annotation.compileTimeOnly
import scala.language.higherKinds
import scala.reflect.macros.blackbox

/** A DSL for the `cats.effect.IO` effect implemented in terms of scalac's -Xasync phase. */
class CatsEffectDsl[M[_]] {
  def async[T](body: T)(implicit M: Effect[M]): M[T] = macro CatsEffectDsl.asyncImpl
  @compileTimeOnly("[async] `await` must be enclosed in `async`")
  def await[T](output: M[T]): T = ???
}

object CatsEffectDsl {
  def apply[M[_]]: CatsEffectDsl[M] = new CatsEffectDsl[M]
  type ThrowableOrValue = Either[Throwable, AnyRef]
  def asyncImpl(c: blackbox.Context)(body: c.Tree)(M: c.Tree): c.Tree = {
    import c.universe._
    val awaitSym = typeOf[CatsEffectDsl[Any]].decl(TermName("await"))
    def mark(t: DefDef): Tree = c.internal.markForAsyncTransform(c.internal.enclosingOwner, t, awaitSym, Map.empty)
    val name = TypeName("stateMachine$async")
    q"""
      final class $name(
          callback: ${typeOf[ThrowableOrValue => Unit]}
        ) extends ${symbolOf[StateMachine[_]]}[M]($M, callback) {

        ${mark(q"""override def apply(tr$$async: ${typeOf[ThrowableOrValue]}): ${typeOf[Unit]} = ${body}""")}
      }
      $M.async[${typeOf[AnyRef]}](cb => new $name(cb)).asInstanceOf[${c.macroApplication.tpe}]
    """
  }

  abstract class StateMachine[M[_]](M: Effect[M], callback: ThrowableOrValue => Unit) extends monadui.AsyncStateMachine[M[AnyRef], ThrowableOrValue] with (ThrowableOrValue => Unit) {
    // FSM translated method
    def apply(tr$async: ThrowableOrValue): Unit

    // Required methods
    protected var state$async: Int = 0
    protected def completeFailure(t: Throwable): Unit = callback.apply(Left(t))
    protected def completeSuccess(value: AnyRef): Unit = callback.apply(Right(value))
    protected def onComplete(f: M[AnyRef]): Unit = {
      M.runAsync(f){either => this(either); IO.unit}.unsafeRunSync()
    }
    protected def getCompleted(f: M[AnyRef]): ThrowableOrValue = {
      // TODO get the immediate value out so we can avoid a context switch.
      null
    }
    protected def tryGet(tr: ThrowableOrValue): AnyRef = tr match {
      case Right(value) =>
        value.asInstanceOf[AnyRef]
      case Left(throwable) =>
        completeFailure(throwable)
        this // sentinel value to indicate the dispatch loop should exit.
    }

    apply(null)
  }
}
