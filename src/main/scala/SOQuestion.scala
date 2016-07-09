
/**
  * Created by Jimmy on 2016/07/09.
  *
  * StackOverflowへの質問用
  */
object SOQuestion {
  def main(args: Array[String]) {

    import scala.reflect.runtime.universe._

    val exp = reify(
      {
        val s = "hello, world"
        val d = 10
      }
    )

    // convert exp.tree
    println(convert(exp.tree))


//    scala.reflect

    // Definition of the Converter
    def convert(tree: Tree): String = tree match {
      // convert "val d = 10" to "int d := 10"
      case ValDef(modifiers, variableName, typeTree, rightSide) =>
        s"[variable type] ${variableName} :=  ${convert(rightSide)};"

      /*
          {
           statementA
           statementB
          }

          into

          begin
            statementA;
            return statementB;
          end
       */
      case Block(codeList, returnValue) =>
        s"""begin
          |${codeList
            .map(convert)    // convert
            .map("    " + _) // indent
            .mkString("\n")}
          |    return ${returnValue};
          |end
          |
        """.stripMargin

      // for example,
      // Literal(Constant("hello, world") into "hello, world"
      case Literal(Constant(any)) =>
        any match {
          case _: String => "\"" + any + "\""
          case _         => any.toString
        }

    // unimplemented, unexpected or unknown tree format
    case tree =>
      "Unknown tree " + showRaw(tree)
  }



  }
}
