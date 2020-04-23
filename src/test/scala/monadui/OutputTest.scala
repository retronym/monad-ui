package monadui

import java.nio.file.{Files, Paths}

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.immutable.HashMap

object OutputTestMain {
  def main(args: Array[String]): Unit = {
    val o = new OutputTest
    import o._
    testFlatMapDsl
    testFlatMapDslWithoutIndyLambda
    testFlatMapMacroDsl
    testImplicitDsl
    testAsyncDsl
  }
}

class OutputTest {
  val List(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10) = (1 to 10).toList.map(i => Output(i, ("Source", "OutputTest")))

  def measureCodeSize(a: Any) = {
    val classPathEntry = Paths.get(a.getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    val entries = Files.walk(classPathEntry).iterator().asScala
    val baseFileName = classPathEntry.resolve(a.getClass.getName.replace('.', '/')).toString
    val matching = entries.filter(_.toString.startsWith(baseFileName)).toList
    val classBytes = matching.map(m => Files.size(m)).sum
    println("=" * 80)
    println(s"""${a} generates ${matching.size} classes, ${classBytes} bytes""")
    println(matching.map(x => classPathEntry.relativize(x)).mkString("\n"))
  }

  object testFlatMapDsl {
    measureCodeSize(this)
    import OutputFlatMapDsl._
    val result = for (_1 <- o1; _2 <- o2; _3 <- o3; _4 <- o4; _5 <- o5; _6 <- o6; _7 <- o7; _8 <- o8; _9 <- o9; _10 <- o10) yield (_1 + _2 + _3 + _4 + _5 + _6 + _7 + _8 + _9 + _10)
    println(result)
  }

  case object testFlatMapDslWithoutIndyLambda {
    measureCodeSize(this)
    import OutputFlatMapDslWithoutIndyLambda._
    val result = for (_1 <- o1; _2 <- o2; _3 <- o3; _4 <- o4; _5 <- o5; _6 <- o6; _7 <- o7; _8 <- o8; _9 <- o9; _10 <- o10) yield (_1 + _2 + _3 + _4 + _5 + _6 + _7 + _8 + _9 + _10)
    println(result)
  }

  case object testFlatMapMacroDsl {
    measureCodeSize(this)
    import OutputFlatMapMacroDsl._
    val result = o1.flatMap(x => new Output[Int](None, HashMap.empty))
    //val result = for (_1 <- o1; _2 <- o2; _3 <- o3; _4 <- o4; _5 <- o5; _6 <- o6; _7 <- o7; _8 <- o8; _9 <- o9; _10 <- o10) yield (_1 + _2 + _3 + _4 + _5 + _6 + _7 + _8 + _9 + _10)
    println(result)
  }

  case object testImplicitDsl {
    measureCodeSize(this)
    import OutputImplicitDsl._
    val result = writing { implicit scope =>
      o1.get + o2.get + o3.get + o4.get + o5.get + o6.get + o7.get + o8.get + o9.get + o10.get
    }
    println(result)
  }

  case object testAsyncDsl {
    measureCodeSize(this)
    import OutputAsyncDsl._
    val result = writing {
      value(o1) + value(o2) + value(o3) + value(o4) + value(o5) + value(o6) + value(o7) + value(o8) + value(o9) + value(o10)
    }
    println(result)
  }
}
