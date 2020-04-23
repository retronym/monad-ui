package monadui

import monadui.OutputImplicitDsl.OutputScope

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.language.experimental.macros

/** Models Writer and Option effects. */
final case class Output[T](value: Option[T], written: HashMap[String, Vector[Any]]) {

  def get(implicit scope: OutputScope): T = {
    scope.append(written)
    value match {
      case Some(x) => x
      case None => throw scope.noSuchElementException
    }
  }
  def withValue[U](value: Option[U], written: HashMap[String, Vector[Any]]): Output[U] = {
    new Output(value, Output.mergeMultiMap(this.written, written))
  }
}

object Output {
  def apply[T](value: T, written: (String, Any)*): Output[T] = {
    new Output(Some(value), toMultiMap[String, Any](written))
  }

  def mergeMultiMap[K, V](m1: HashMap[K, Vector[V]], m2: HashMap[K, Vector[V]]): HashMap[K, Vector[V]] = {
    if (m1.isEmpty) m2 else if (m2.isEmpty) m1 else {
      m1.merged(m2) {
        case ((k1, v1), (_, v2)) => (k1, v1 ++ v2)
      }
    }
  }

  private def toMultiMap[K, V](written: Seq[(K, V)]): HashMap[K, Vector[V]] = {
    val mutableMap = collection.mutable.HashMap[K, mutable.Builder[V, Vector[V]]]()
    for ((k, v) <- written) mutableMap.getOrElseUpdate(k, Vector.newBuilder[V]) += v
    val immutableMapBuilder = collection.immutable.HashMap.newBuilder[K, Vector[V]]
    immutableMapBuilder ++= mutableMap.mapValues(_.result())
    immutableMapBuilder.result()
  }
}
