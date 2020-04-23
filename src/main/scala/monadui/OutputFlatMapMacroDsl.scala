package monadui

import language.experimental.macros
import scala.reflect.macros.blackbox

/** The same API as FlatMapDsl, but inlines the combinators when the argument is function literal. */
object OutputFlatMapMacroDsl {
  implicit class RichOutput[T](val output: Output[T]) extends AnyVal {
    def flatMap[U](f: T => Output[U]): Output[U] = macro flatMapImpl[U]
    def map[U](f: T => U): Output[U] = macro mapImpl[U]
    def filter(f: T => Boolean): Output[T] = ???
    def withFilter(f: T => Boolean): Output[T] = ???
  }
  // TODO add @unheckedBounds to the temp vals to avoid refchecks warnings that otherwise would not have been
  //      issued for sub-expressions that have inferred types that don't satisfy bounds.
  def flatMapImpl[U: c.WeakTypeTag](c: blackbox.Context)(f: c.Tree): c.Tree = {
    import c.universe._
    c.macroApplication match {
      case Apply(TypeApply(Select(Apply(_, List(qual)), _), _), _) =>
        f match {
          case Function(param :: Nil, rhs) =>
            import c.internal.decorators._

            val tempQual = c.internal.enclosingOwner.newTermSymbol(c.freshName(TermName("qual$"))).setInfo(qual.tpe)
            val valueSym = typeOf[Output[Any]].member(TermName("value"))
            val tempQualValueType = valueSym.typeSignatureIn(qual.tpe).resultType
            val tempQualValue = c.internal.enclosingOwner.newTermSymbol(c.freshName(TermName("value$"))).setInfo(tempQualValueType)
            val outputUType = weakTypeOf[Output[U]]
            val otherOutput = c.internal.enclosingOwner.newTermSymbol(c.freshName(TermName("o1$"))).setInfo(outputUType)
            val inlinedRhs = internal.substituteSymbols(rhs, param.symbol :: Nil, tempQualValue :: Nil)
            val tree = q"""
                ${internal.valDef(tempQual, internal.changeOwner(qual, qual.symbol.owner, tempQual))}
                if ($tempQual.value.isDefined) {
                  ${internal.valDef(tempQualValue, q"$tempQual.value")}
                  ${internal.valDef(otherOutput, inlinedRhs)}
                  $tempQual.withValue[${symbolOf[U]}]($otherOutput.value, $otherOutput.written)
                } else {
                  this.asInstanceOf[${outputUType}]
                }
               """
            tree
          case _ =>
            q"new _root_.monadui.OutputFlatMapDsl($qual).flatMap($f)"
        }
      case _ =>
        c.abort(c.macroApplication.pos, "unable to find receiver")
    }
  }
  def mapImpl[U: c.WeakTypeTag](c: blackbox.Context)(f: c.Tree): c.Tree = {
    import c.universe._
    c.macroApplication match {
      case Apply(TypeApply(Select(Apply(_, List(qual)), _), _), _) =>
        f match {
          case Function(param :: Nil, rhs) =>
            import c.internal.decorators._

            val tempQual = c.internal.enclosingOwner.newTermSymbol(c.freshName(TermName("qual"))).setInfo(qual.tpe)
            val valueSym = typeOf[Output[Any]].member(TermName("value"))
            val tempQualValueType = valueSym.typeSignatureIn(qual.tpe).resultType
            val tempQualValue = c.internal.enclosingOwner.newTermSymbol(c.freshName(TermName("value"))).setInfo(tempQualValueType)
            val inlinedRhs = internal.substituteSymbols(rhs, param.symbol :: Nil, tempQualValue :: Nil)
            val tree = q"""
                ${internal.valDef(tempQual, internal.changeOwner(qual, qual.symbol.owner, tempQual))}
                if ($tempQual.value.isDefined) {
                  ${internal.valDef(tempQualValue, q"$tempQual.value")}
                  $tempQual.withValue(${definitions.SomeModule}(${inlinedRhs}))
                } else {
                  this.asInstanceOf[${weakTypeOf[Output[U]]}]
                }
               """
            tree
          case _ =>
            q"new _root_.monadui.OutputFlatMapDsl($qual).map($f)"
        }
      case _ =>
        c.abort(c.macroApplication.pos, "unable to find receiver")
    }
  }
}
