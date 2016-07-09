import scala.collection.immutable.::
import scala.reflect.NameTransformer
import scala.reflect.runtime.universe._

/**
  * Created by Ryo on 2016/07/10.
  */
class ToPraatConverter(nonTyped: Tree, indentSpace: String = "\t") {

  import scala.reflect.runtime.{currentMirror => cm}
  import scala.tools.reflect.ToolBox


  // one time parse
  lazy val praatScript: String = {
    val typedTree = cm.mkToolBox().typecheck(nonTyped)
    parseTypedTree(typedTree)
  }

  // Scalaのメソッドとpraatの関数(?)との対応
  def scalaMethodNameToPraatName = Map(
    "toPitch" -> "To Pitch",
    "getNumberOfIntervals" -> "Get number of intervals",
    "toIntensity" -> "To Intensity",
    "getLabelOfInterval" -> "Get label of interval",
    "getStartingPoint" -> "Get starting point",
    "getEndPoint" -> "Get end point",
    "getValueAtTime" -> "Get value at time",
    "getMaximum" -> "Get maximum"
  )

  // Scalaのメソッドからpraatのトップレベルの関数への対応
  def scalaMethodNameToPratFunctionName = Map(
    "fileAppend" -> "fileappend"
  )


  // 変数の情報
  case class VariableInfo(varName: String,
                          typeTree: Tree,
                          isString: Boolean // "Sound assingment4"などをオブジェクト扱うときがあるが、これはpraat内では文字列表現を使うのでtrueにする
                         )

  // 変数情報を積み込んでいく
  var variableInfos: List[VariableInfo] = List.empty

  private[this] def parseTypedTree(tree: Tree): String = {



    tree match {

      case Block(ModuleDef(_, _, Template(_, _, defaultConstructor :: codes))::_ , _) =>

        codes.map(parseTypedTree).mkString("\n") // TODO parseTreesにする

      case ValDef(_, TermName(varName), typeTree, Apply(Select(Ident(termName), TermName("apply")), List(Literal(Constant(fileName))))) =>


        // なぜか変数名の最後に空白ができるので、削除
        val stripVarName = varName.stripSuffix(" ")

        // 変数の情報を追加
        variableInfos :+= VariableInfo(stripVarName, typeTree, isString = true)


        termName.toString match {
          case "SoundByFile" =>
            s"""${stripVarName}$$ = "Sound ${fileName}""""
          case "TextGridByFile" =>
            s"""${stripVarName}$$ = "TextGrid ${fileName}""""

          case _ => "dummmy-- "
        }

      case ValDef(_, TermName(varName), typeTree, Apply(Select(Select(This(TypeName(_)), reciever@TermName(recieverName)), TermName(methodName)), params)) =>


        val stripedVarName = varName.stripSuffix(" ")

        val varIsString = typeTree.toString() == "String"

        // 変数情報を追加
        variableInfos :+= VariableInfo(stripedVarName, typeTree, isString = varIsString)


        // レシーバーの情報を取得
        val recieverInfoOpt = variableInfos.find(_.varName == recieverName)

        recieverInfoOpt match {
          case Some(reciever) =>
            scalaMethodNameToPraatName.get(methodName) match {
              case Some(praatFuncName) =>
                s"""|selectObject: ${recieverName}${if(reciever.isString) "$" else ""}
                    |${stripedVarName}${if(varIsString) "$" else ""} = ${praatFuncName}... ${params.map(parseTypedTree).mkString(" ")}
                    |""".stripMargin
              case None =>
                s"#Unknown function ${methodName}   " + unknownTree(tree)
            }

          case None =>
            // レシーバーがvariableInfosに存在しないとき
            unknownTree(tree)
        }




      case ValDef(Modifiers(_), TermName(varName), typeTree, rightTree) =>

        // 変数の型が文字列かどうか
        val isString = typeTree.toString() == "java.lang.String" // TODO 文字列の比較ではない方法で判定すべきだと思う

        // 変数情報を追加
        variableInfos :+= VariableInfo(varName, typeTree, isString = isString)

        // variable name, considering string
        // in praat string variable has suffix $
        val varNameConsideringStr = if(isString) varName+"$" else varName

        s"""${varNameConsideringStr} = ${parseTypedTree(rightTree)}"""

      case DefDef(modifiers, TermName(funcName), _, _, typeTree, righSide) =>
        variableInfos.find(_.varName == funcName) match {
          // なぜか変数と同じ名前の関数も定義されるので、その時は無視する
          case Some(_) => ""
          case None => unknownTree(tree)
        }


      case Literal(Constant(c)) =>
        c match{
          case ()   => ""
          case true => "yes"
          case false => "no"
          case _ : String => "\"" + c + "\""
          case _ => c.toString
        }

      case Block(codes, returnValue) =>
        (codes ++ List(returnValue)).map(parseTypedTree).mkString("\n").split("\n").map(indentSpace + _).mkString("\n") +"\n"

      case Ident(TermName(varName)) =>

        variableInfos.find(_.varName == varName) match {
          case Some(VariableInfo(_, _, isString)) =>
            varName.toString + (if(isString) "$" else "")
          case None =>
            varName // Hertzなどはここに引っかかる
        }


      case Apply(TypeApply(Select(Apply(Select(Apply(Select(Ident(predef), TermName("intWrapper")), List(  loopFrom  )), TermName("to")), List( loopTo  )), TermName("foreach")), List(TypeTree())), List(Function(List(ValDef(modifiers, TermName(counterName), TypeTree(), EmptyTree)), block   ))) =>

        variableInfos :+= VariableInfo(counterName, TypeTree(), isString = false) // TODO TypeTree()ではなく本当は型はIntを入れたい
        s"""
           |for ${counterName} from ${parseTypedTree(loopFrom)} to ${parseTypedTree(loopTo)}
           |${parseTypedTree(block)}
           |endfor
           |""".stripMargin

      // labelNumbersでいいところを冗長にAssingment.this.labelNumbersになっている変数を簡素にする
      case Select(This(TypeName(_)), TermName(varName)) =>
        varName

      case Apply(Select(Apply(Select(Apply(Select(Ident(_), TermName("intWrapper")), List(loopFrom)), TermName("to")), List(Select(This(TypeName(_)), TermName(loopTo)))), TermName("foreach")), List(Function(List(ValDef(Modifiers(_), TermName(counterName), TypeTree(), EmptyTree)), block))) =>
        variableInfos :+= VariableInfo(counterName, TypeTree(), isString = false) // TODO TypeTree()ではなく本当は型はIntを入れたい
        s"""
           |for ${counterName} from ${loopFrom} to ${loopTo}
           |${parseTypedTree(block)}
           |endfor
           |""".stripMargin

      // 引用符「""」を囲まない文字列
      case Apply(Select(Select(Ident(TermName("package")), TermName("string2RawString")), TermName("apply")), List(rawString)) =>
        val embeded = rawString match {
          case Literal(Constant(string)) =>
            string.toString
          // use stirng context
          case Apply(Select(Apply(Select(Ident(TermName("StringContext")), TermName("apply")), midStrList), TermName("s") ), varNameList) =>

            val (Literal(Constant(last))) = midStrList.last
            ((midStrList zip varNameList).map{case (Literal(Constant(midStr)), Ident(TermName(varName))) =>
              variableInfos.find(_.varName == varName) match {
                case Some(VariableInfo(_, _, isString)) =>
                  s"${midStr}'${varName.toString}${if(isString) "$" else ""}'"
                case None => "#Unknow Variable unexpcted " +unknownTree(tree)
              }
            }.mkString("") + last)
              .replaceAll("\\\\t", "\t").replaceAll("\\\\n", "\n") // s""を使った時はタブが文字の\tになるのでそれをタブにする

          case _ => unknownTree(tree)
        }

        embeded
          .replaceAll("\\t", "'tab\\$'") // タブをtab$にする
          .replaceAll("\n", "'newline\\$'") // 改行を改行文字変換


      // top level function
      case exp@Apply(Select(Ident(TermName("package")) , TermName(scalaFuncName)), args) =>
        scalaMethodNameToPratFunctionName.get(scalaFuncName) match {
          case Some(funcName) =>
            s"${funcName} ${args.map(parseTypedTree).mkString(" ")}"
          case None => unknownTree(tree)
        }

      // operator - 演算子
      case exp@Apply(Select(leftHand , TermName(op)), List(rightHand)) =>

        // $minus => 「-」などの変換をする
        val opName = NameTransformer.decode(op) match {
          case "!=" => "<>"
          case "==" => "="
          case e    => e
        }


        s"""(${parseTypedTree(leftHand)} ${opName} ${parseTypedTree(rightHand)})"""

      case If(condition, trueTree, falseTree) =>
        s"""if ${parseTypedTree(condition)}
            |${parseTypedTree(trueTree)}
            |else
            |${parseTypedTree(falseTree)}
            |endif
         """.stripMargin


      case exp =>
        unknownTree(exp)
    }
  }

  private[this] def unknownTree(exp: Tree)= {
    s"# Unknown ${showRaw(exp)}\n"
  }
}
