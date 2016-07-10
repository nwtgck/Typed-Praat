import scala.io.Source
import scala.reflect.runtime.universe._

/**
  * Created by Jimmy on 2016/07/08.
  */
object Main2 {
  def main(args: Array[String]) {

    val fileContent = Source.fromFile("./src/main/scala/testScripts/Assignment4.scala")
      .mkString("").split("\n").filter(!_.startsWith("package")).mkString("\n") // packageから始まる行を除外する

    import scala.tools.reflect.ToolBox
    val tb = runtimeMirror(getClass.getClassLoader).mkToolBox()

    val parsedTree = tb.parse(fileContent)

    val converter = new ToPraatConverter(parsedTree)
    println(converter.praatScript)

  }
}
