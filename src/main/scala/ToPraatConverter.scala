import praat.annotation.PraatName

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
      .split("\n").map(_.stripPrefix(indentSpace)).mkString("\n") // インデントが余計にあるので削除
  }


  // 変数の情報
  case class VariableInfo(varName: String,
                          typeTree: Tree,
                          isString: Boolean // "Sound assingment4"などをオブジェクト扱うときがあるが、これはpraat内では文字列表現を使うのでtrueにする
                         )

  // 変数情報を積み込んでいく
  var variableInfos: List[VariableInfo] = List.empty

  private[this] def parseTypedTree(tree: Tree): String = {


    // selectObjectするやつ（2回使うので、関数化）
    def selectObject(typeTree: Tree, stripedVarName: String, recieverName: String, methodName: String, params: List[Tree]) = {
      val varIsString = typeTree.toString() == "String"

      // 変数情報を追加
      variableInfos :+= VariableInfo(stripedVarName, typeTree, isString = varIsString)


      // レシーバーの情報を取得
      val recieverInfoOpt = variableInfos.find(_.varName == recieverName)

      recieverInfoOpt match {
        case Some(recieverInfo) =>

          // レシーバーのクラスのパス名を取得 TODO .toStringしない方法でやりたい
          val recieverClassName = recieverInfo.typeTree.toString
          // Methodオブジェクトを取得
          val methodOpt = Class.forName(recieverClassName).getMethods.find(_.getName == methodName)

          methodOpt match {
            case Some(method) =>
              val praatNameOpt = Option(method.getAnnotation(classOf[PraatName]))
              praatNameOpt match {
                case Some(praatName) =>
                  val praatFuncName = praatName.name()
                  s"""|selectObject: ${recieverName}${if(recieverInfo.isString) "$" else ""}
                      |${stripedVarName}${if(varIsString) "$" else ""} = ${praatFuncName}${if(params.isEmpty) "" else "... "}${params.map(parseTypedTree).mkString(" ")}
                      |""".stripMargin
                case None =>
                  s"#Unknown function ${recieverClassName}.${methodName}" + unknownTree(tree)
              }
            case None =>
              println("hello")
              s"#Unexpected ${recieverInfo.typeTree}.$methodName"
          }

//          scalaMethodNameToPraatName.get(methodName) match {
//            case Some(praatFuncName) =>
//              s"""|selectObject: ${recieverName}${if(recieverInfo.isString) "$" else ""}
//                  |${stripedVarName}${if(varIsString) "$" else ""} = ${praatFuncName}... ${params.map(parseTypedTree).mkString(" ")}
//                  |""".stripMargin
//            case None =>
//              s"#Unknown function ${methodName}   " + unknownTree(tree)
//          }

        case None =>
          // レシーバーがvariableInfosに存在しないとき
          unknownTree(tree)
      }
    }

    val raw = showRaw(tree)
    tree match {


//      case ModuleDef(Modifiers(), TermName("Assignment4"), Template(List(Select(Ident(scala), TypeName("AnyRef"))), noSelfType, List(DefDef(Modifiers(), termNames.CONSTRUCTOR, List(), List(List()), TypeTree(), Block(List(Apply(Select(Super(This(TypeName("Assignment4")), typeNames.EMPTY), termNames.CONSTRUCTOR), List())), Literal(Constant(())))))))


      case ModuleDef(_, _, Template(_, _, defaultConstructor :: codes)) =>

        codes.map(parseTypedTree).mkString("\n") // TODO parseTreesにする

//      case ValDef(modifiers, TermName(varName), typeTree, Apply(Ident(TermName(term)), List(Literal(Constant(fileName))))) =>
//
//        // なぜか変数名の最後に空白ができるので、削除
//        val stripVarName = varName.stripSuffix(" ")
//
//        // 変数の情報を追加
//        variableInfos :+= VariableInfo(stripVarName, typeTree, isString = true)
//
//
//        term match {
//          case "SoundByFile" =>
//            s"""${stripVarName}$$ = "Sound ${fileName}""""
//          case "TextGridByFile" =>
//            s"""${stripVarName}$$ = "TextGrid ${fileName}""""
//
//          case _ => "dummmy-- "
//        }


      // ParablicやHertzなどのため
      case Select(Ident(TermName("praat")), name) =>
        name.toString

      case ValDef(modifiers4, TermName(varName), typeTree, Apply(Select(Select(Ident(TermName("praat")), TermName(term)), TermName("apply")), List(Literal(Constant(fileName))))) =>

        // なぜか変数名の最後に空白ができるので、削除
        val stripVarName = varName.stripSuffix(" ")

        // 変数の情報を追加
        variableInfos :+= VariableInfo(stripVarName, typeTree, isString = true)


        term.toString match {
          case "SoundByFile" =>
            s"""${stripVarName}$$ = "Sound ${fileName}""""
          case "TextGridByFile" =>
            s"""${stripVarName}$$ = "TextGrid ${fileName}""""

          case "IntensityByFile" =>
            s"""${stripVarName}$$ = "Intensity ${fileName}""""

          case _ => "dummmy-- "
        }

//      case ValDef(_, TermName(varName), typeTree, Apply(Select(Ident(termName), TermName("apply")), List(Literal(Constant(fileName))))) =>
//
//
//        // なぜか変数名の最後に空白ができるので、削除
//        val stripVarName = varName.stripSuffix(" ")
//
//        // 変数の情報を追加
//        variableInfos :+= VariableInfo(stripVarName, typeTree, isString = true)
//
//
//        termName.toString match {
//          case "SoundByFile" =>
//            s"""${stripVarName}$$ = "Sound ${fileName}""""
//          case "TextGridByFile" =>
//            s"""${stripVarName}$$ = "TextGrid ${fileName}""""
//
//          case _ => "dummmy-- "
//        }


      // selectObjectしなくては行けない時（implicitなどを使ったメソッドを束縛）
      case ValDef(_, TermName(varName), typeTree, Apply(Apply(TypeApply(Select(Select(thisMod, TermName(recieverName)), TermName(methodName)), List(TypeTree())), params), List(TypeApply(Select(Select(This(TypeName("scala")), predef), TermName("$conforms")), List(TypeTree()))))) =>
        val stripedVarName = varName.stripSuffix(" ")
        selectObject(typeTree, stripedVarName, recieverName, methodName, params)


      // selectObjectしなくてはいけない時(implicitなどの使っていないメソッドを束縛）
      case ValDef(_, TermName(varName), typeTree, Apply(Select(Select(This(TypeName(_)), reciever@TermName(recieverName)), TermName(methodName)), params)) =>
        val stripedVarName = varName.stripSuffix(" ")
        selectObject(typeTree, stripedVarName, recieverName, methodName, params)




      case ValDef(Modifiers(_), TermName(_varName), typeTree, rightTree) =>

        val stripedVarName = _varName.stripSuffix(" ")

        // 変数の型が文字列かどうか
        val isString = typeTree.toString() == "String" // TODO 文字列の比較ではない方法で判定すべきだと思う

        // 変数情報を追加
        variableInfos :+= VariableInfo(stripedVarName, typeTree, isString = isString)

        // variable name, considering string
        // in praat string variable has suffix $
        val varNameConsideringStr = if(isString) stripedVarName+"$" else stripedVarName

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

        // treeから型を知ることができる =:=ではな<:<を使うと思ったように判定できた
        if(false) {
          tree.getClass.getMethods.find(_.getName == "tpe") match {
            case Some(method) =>
              println(method)
              val invoked = method.invoke(tree)
              import scala.reflect.runtime.universe._
              val type2 = invoked.asInstanceOf[Type]
              val eq = type2 <:< typeOf[String]
              val eq2 = type2 <:< typeOf[Int]
              val eq3 = type2 <:< typeOf[Double]
              println(eq, invoked)
            case None =>
              println("none")
          }
        }

        variableInfos.find(_.varName == varName) match {
          case Some(VariableInfo(_, _, isString)) =>
            varName.toString + (if(isString) "$" else "")
          case None =>
            varName // Hertzなどはここに引っかかる
        }



//      case Apply(TypeApply(Select(Apply(Select(Apply(Select(Select(This(TypeName("scala")), scala.Predef), TermName("intWrapper")), List(Literal(Constant(1)))), TermName("to")), List(Select(This(TypeName("Assignment4")), TermName("numberOfLabels")))), TermName("foreach")), List(TypeTree())), List(Function(List(ValDef(modifiers, TermName("i"), TypeTree(), EmptyTree)), Literal(Constant(())))))


      case Apply(TypeApply(Select(Apply(Select(Apply(     Select(Select(This(TypeName("scala")), predef), TermName("intWrapper"))  , List(  loopFrom  )), TermName("to")), List( loopTo  )), TermName("foreach")), List(TypeTree())), List(Function(List(ValDef(modifiers, TermName(counterName), TypeTree(), EmptyTree)), block   ))) =>

        //      case Apply(TypeApply(Select(Apply(Select(Apply(Select(Ident(predef), TermName("intWrapper")), List(  loopFrom  )), TermName("to")), List( loopTo  )), TermName("foreach")), List(TypeTree())), List(Function(List(ValDef(modifiers, TermName(counterName), TypeTree(), EmptyTree)), block   ))) =>

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
      case Apply(Select(Select(Select(Ident(TermName("praat")), termNames.PACKAGE), TermName("string2RawString")), TermName("apply")), List(rawString)) =>

        val embeded = rawString match {
          case Literal(Constant(string)) =>
            string.toString
          // use stirng context
          case Apply(Select(Apply(Select(Select(Ident(TermName("scala")), TermName("StringContext")), TermName("apply")), midStrList), TermName("s") ), varNameList) =>


            def joinMidStrAndVarName(midStr: Any, varName: String) ={
              variableInfos.find(_.varName == varName) match {
                case Some(VariableInfo(_, _, isString)) =>
                  s"${midStr}'${varName.toString}${if(isString) "$" else ""}'"
                case None => "#Unknow Variable unexpcted " +unknownTree(tree)
              }
            }

            val (Literal(Constant(last))) = midStrList.last
            ((midStrList zip varNameList).map{
              case (Literal(Constant(midStr)), Ident(TermName(varName))) =>
                joinMidStrAndVarName(midStr, varName)

              case (Literal(Constant(midStr)), Select(This(TypeName(_)), TermName(varName))) =>
                joinMidStrAndVarName(midStr, varName)

              case unexpected =>
                s"#Unexpected string context ${unexpected} ${showRaw(unexpected)}"

            }.mkString("") + last)
              .replaceAll("\\\\t", "\t").replaceAll("\\\\n", "\n") // s""を使った時はタブが文字の\tになるのでそれをタブにする

          case _ => "OK" + unknownTree(tree)
        }

        embeded
          .replaceAll("\\t", "'tab\\$'") // タブをtab$にする
          .replaceAll("\n", "'newline\\$'") // 改行を改行文字変換


      // top level function
      case exp@Apply(Select(Select(Ident(TermName("praat")), termNames.PACKAGE) , TermName(scalaFuncName)), args) =>

        Class.forName("praat.package").getMethods.find(_.getName == scalaFuncName) match {
          case Some(method) =>
            val praatNameOpt = Option(method.getAnnotation(classOf[PraatName]))
            praatNameOpt match {
              case Some(praatName) =>
                val funcName = praatName.name()
                s"${funcName} ${args.map(parseTypedTree).mkString(" ")}"
              case None =>
                s"#Unset PraatName annotation for ${scalaFuncName} " + unknownTree(tree)
            }
          case None =>
            s"#Unknow method ${scalaFuncName} " + unknownTree(tree)
        }

//
//        //      case exp@Apply(Select(Ident(TermName("package")) , TermName(scalaFuncName)), args) =>
//        scalaMethodNameToPratFunctionName.get(scalaFuncName) match {
//          case Some(funcName) =>
//            s"${funcName} ${args.map(parseTypedTree).mkString(" ")}"
//          case None => s"#Unknow method ${scalaFuncName} " + unknownTree(tree)
//        }

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

      // ingnore import
      case Import(_, selections) =>
        ""

      case exp =>
        unknownTree(exp)
    }
  }

  private[this] def unknownTree(exp: Tree)= {
    s"# Unknown ${showRaw(exp)}  ${exp}\n"
  }
}
