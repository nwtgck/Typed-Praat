import praat.annotation.PraatName

import scala.io.Source
import scala.reflect.runtime.universe._

/**
  * Created by Jimmy on 2016/07/08.
  */
object Main {
  def main(args: Array[String]) {

    val fileContent = Source.fromFile("./src/main/scala/testScripts/Test2.scala")
      .mkString("").split("\n").filter(!_.startsWith("package")).mkString("\n") // packageから始まる行を除外する

    import scala.tools.reflect.ToolBox
    val tb = runtimeMirror(getClass.getClassLoader).mkToolBox()

    val parsedTree = tb.parse(fileContent)


//    println(showRaw(tb.typecheck(parsedTree)))

    val converter = new ToPraatConverter(parsedTree)
    println(converter.praatScript)

  }
}
