import scala.collection.immutable.::
import scala.reflect.NameTransformer
//import scala.reflect.api.Trees.{BlockExtractor, ModuleDefExtractor, TemplateExtractor}
import scala.reflect.runtime.universe._

/**
  * Created by Jimmy on 2016/07/08.
  */
object Main2 {
  def main(args: Array[String]) {

    import praat._

    val exp = reify{
      object Assignment4{
        val soundAssinment4 = SoundByFile("Assignment4")
        val textGrid        = TextGridByFile("Assignment4")
        val pitch           = soundAssinment4.toPitch(0.0, 7.5, 600.0)
////
        val numberOfLabels  = textGrid.getNumberOfIntervals(1)
        val intensity       = soundAssinment4.toIntensity(75.0, 0.0, true)

        for(i <- 1 to numberOfLabels){
          val label = textGrid.getLabelOfInterval(1, i)
          if(label != ""){
            val start = textGrid.getStartingPoint(1, i)
            val end   = textGrid.getEndPoint(1, i)
            val mid   = 0.5 * (start + end)
            val meanf = pitch.getValueAtTime(mid, Hertz, Linear)
            val maxIntensity = intensity.getMaximum(start, end, Parabolic)
            val duration = end - start
          }

        }
      }
    }

//    println(showRaw(exp.tree))

    val ifTree = reify{
      val a = 10
      if(a == 10){
        val b = 20
      }
    }

//    println(showRaw(ifTree))

//    println(parseTree(exp.tree))

//    println(exp)

    import scala.reflect.runtime.{currentMirror => cm}
    import scala.tools.reflect.ToolBox

//    val Block(ModuleDef(_, _, Template(_, _, defaultConstructor :: codes))::_ , _) = cm.mkToolBox().typecheck(exp.tree)

//    println(exp.tree)
//    println(codes.map{(e)=> (e.getClass.getMethod("toString"), e.tpe)})

//    codes.foreach(println)
//
//    val head = codes.head
//
//    val ValDef(Modifiers(_), TermName(varName), typeTree1, Apply(Select(Ident(companion), TermName("apply")), List(Literal(Constant("Assignment4"))))) = head
//
////    println(typeTree1)
//



    val typedTree = cm.mkToolBox().typecheck(exp.tree)
    println(parseTypedTree(typedTree))
    println(variableInfos)


//    def typeCheck(tree: Tree) = cm.mkToolBox().typecheck(tree)
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


  // 変数の情報
  case class VariableInfo(varName: String,
                          typeTree: Tree,
                          isString: Boolean // "Sound assingment4"などをオブジェクト扱うときがあるが、これはpraat内では文字列表現を使うのでtrueにする
                         )

  import scala.reflect.runtime.{currentMirror => cm}

  def typeCheck(tree: Tree) = tree // if(tree.isType) tree else cm.mkToolBox().typecheck(tree)

  // 変数情報を積み込んでいく
  var variableInfos: List[VariableInfo] = List.empty

  def parseTypedTree(tree: Tree): String = {


//    val typeCheckAndParse = typeCheck _ andThen parseTypedTree _

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

        // 変数情報を追加
        variableInfos :+= VariableInfo(stripedVarName, typeTree, isString = false)


        // レシーバーの情報を取得
        val recieverInfoOpt = variableInfos.find(_.varName == recieverName)

        recieverInfoOpt match {
          case Some(reciever) =>
            scalaMethodNameToPraatName.get(methodName) match {
              case Some(praatFuncName) =>
                s"""|selectObject: ${recieverName}${if(reciever.isString) "$" else ""}
                    |${stripedVarName} = ${praatFuncName}... ${params.map(parseTypedTree).mkString(" ")}
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

      case DefDef(modifiers, TermName(funcName), List(), List(), TypeTree(), righSide) =>
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
        (codes ++ List(returnValue)).map(parseTypedTree).mkString("\n").split("\n").map("\t" + _).mkString("\n") +"\n"

      case Ident(TermName(varName)) =>
        varName.toString



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


      case exp@Apply(Select(leftHand , TermName(op)), List(rightHand)) =>

        // $minus => 「-」などの変換をする
        val opName = {
          val _opName = NameTransformer.decode(op)
          if (_opName == "!=") "<>" else _opName
        }

        s"""(${parseTypedTree(leftHand)} ${opName} ${parseTypedTree(rightHand)})"""

      case If(condition, trueTree, falseTree) =>
        s"""if ${parseTypedTree(condition)}
           |${parseTypedTree(trueTree)}
           |else
           |${parseTypedTree(falseTree)}
           |endif
         """.stripMargin


      //    case If(Literal(Constant(true)), Literal(Constant(())), Literal(Constant(()))) =>


      //    case If(condition, Literal(Constant(())), Literal(Constant(()))) =>
      //
      //      "\n" + condition.toString()
      //
      ////      "\naaa"

      case exp =>
        unknownTree(exp)
    }
  }

//  def parseTree(tree: Tree): String = tree match {
//
//    case Block(ModuleDef(_, _, Template(_, _, defaultConstructor :: codes))::_ , _) =>
////      parseTree(head)
//      // codeが解析したいコードのリスト
//      codes.map(parseTree).mkString("\n") // TODO parseTreesにする
////    case  =>
//
//
////      "dummy --- "
//
//
//    case ValDef(_, TermName(varName), _, Apply(Select(Ident(termName), TermName("apply")), List(Literal(Constant(fileName))))) =>
//
//      termName.toString match {
//        case "SoundByFile" =>
//          s"""${varName}$$ = "Sound ${fileName}""""
//        case "TextGridByFile" =>
//          s"""${varName}$$ = "TextGrid ${fileName}""""
//
//        case _ => "dummmy-- "
//      }
//
//    case ValDef(_, TermName(varName), TypeTree(), Apply(Select(Select(This(TypeName(_)), TermName(recieverName)), TermName(methodName)), params)) =>
//      s"""|selectObject: ${recieverName}
//         |${varName} = ${methodName} ${params.map(parseTree).mkString(" ")}
//         |""".stripMargin
//
//    case ValDef(Modifiers(_), TermName(varName), TypeTree(), rightTree) =>
//      s"""${varName} = ${parseTree(rightTree)}"""
//
//
//    case Literal(Constant(c)) =>
//      c match{
//        case ()   => ""
//        case true => "yes"
//        case false => "no"
//        case _ => c.toString
//      }
//
//    case Block(codes, returnValue) =>
//      (codes ++ List(returnValue)).map(parseTree).mkString("\n").split("\n").map("\t" + _).mkString("\n") +"\n"
//
//    case Ident(TermName(varName)) =>
//      varName.toString
//
//
//
//    case Apply(Select(Apply(Select(Apply(Select(Ident(_), TermName("intWrapper")), List(loopFrom)), TermName("to")), List(Select(This(TypeName(_)), TermName(loopTo)))), TermName("foreach")), List(Function(List(ValDef(Modifiers(_), TermName(counterName), TypeTree(), EmptyTree)), block))) =>
//      s"""
//         |for ${counterName} from ${loopFrom} to ${loopTo}
//        |${parseTree(block)}
//        |endfor
//        |""".stripMargin
//
//
//    case exp@Apply(Select(leftHand , op), List(rightHand)) =>
//
//      println(
//        s"""
//          |
//          |
//          |${exp.tpe}
//          |
//          ${Select(leftHand , op)}
//          |
//          |
//        """.stripMargin)
//
//      val opNameOpt = op match {
//        case TermName("$minus") => Some("-")
//        case TermName("$plus") => Some("+")
//        case TermName("$times") => Some("*")
//        case _ => None
//      }
//      opNameOpt match{
//        case Some(opName)=>
//          s"""(${parseTree(leftHand)} ${opName} ${parseTree(rightHand)})"""
//        case _ => unknownExp(exp)
//      }
//
////    case If(Literal(Constant(true)), Literal(Constant(())), Literal(Constant(()))) =>
//
//
////    case If(condition, Literal(Constant(())), Literal(Constant(()))) =>
////
////      "\n" + condition.toString()
////
//////      "\naaa"
//
//    case exp =>
//      unknownExp(exp)
//  }

  def unknownTree(exp: Tree)= s"# Unknown ${showRaw(exp)}\n"
}
