import praat.{Hertz, SoundByFile, TextGridByFile}

import scala.collection.immutable.::
import scala.reflect.internal.Trees
import scala.reflect.runtime._
//import scala.reflect.api.Trees.{BlockExtractor, ModuleDefExtractor, TemplateExtractor}
import scala.reflect.runtime.universe._

/**
  * Created by Jimmy on 2016/07/08.
  */
object Main {
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
//          if(label != ""){
          val start = textGrid.getStartingPoint(1, i)
          val end   = textGrid.getEndPoint(1, i)
          val mid   = 0.5 * (start + end)
          val meanf = pitch.getValueAtTime(mid, Hertz, Linear)
          val maxIntensity = intensity.getMaximum(start, end, Parabolic)
          val duration = end - start
//          }
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

    import scala.tools.reflect.ToolBox // requires scala-compiler.jar
    import scala.tools.reflect.ToolBox
    import scala.reflect.runtime.{currentMirror=>cm}

    val Block(ModuleDef(_, _, Template(_, _, defaultConstructor :: codes))::_ , _) = cm.mkToolBox().typecheck(exp.tree)

//    println(exp.tree)
//    println(codes.map{(e)=> (e.getClass.getMethod("toString"), e.tpe)})

    codes.foreach(println)

    val head = codes.head

    val ValDef(Modifiers(_), TermName(varName), typeTree1, Apply(Select(Ident(companion), TermName("apply")), List(Literal(Constant("Assignment4"))))) = head

//    println(typeTree1)

    val typedTree = cm.mkToolBox().typecheck(exp.tree)




    //    println(showRaw(typedTree, printTypes=true))
    //    println(typedTree.asInstanceOf[TypeTree])
    println(parseTypedTree(typedTree))



//    def typeCheck(tree: Tree) = cm.mkToolBox().typecheck(tree)
  }


  import scala.tools.reflect.ToolBox
  import scala.reflect.runtime.{currentMirror=>cm}

  def typeCheck(tree: Tree) = tree // if(tree.isType) tree else cm.mkToolBox().typecheck(tree)

  def parseTypedTree(tree: Tree): String = {


    val typeCheckAndParse = typeCheck _ andThen parseTypedTree _

    tree match {

      case Block(ModuleDef(_, _, Template(_, _, defaultConstructor :: codes))::_ , _) =>
        //      parseTree(head)
        // codeが解析したいコードのリスト


        codes.map(typeCheckAndParse).mkString("\n") // TODO parseTreesにする
      //    case  =>


      //      "dummy --- "


      //    case DefDef(Modifiers(_), TermName(varName), List(), List(), TypeTree(), Select(This(TypeName("Assignment4")), TermName("textGrid ")))

      case ValDef(_, TermName(varName), typeTree, Apply(Select(Ident(termName), TermName("apply")), List(Literal(Constant(fileName))))) =>


        // なぜか変数名の最後に空白ができるので、削除
        val stripVarName = varName.stripSuffix(" ")

//        println(tree)

        termName.toString match {
          case "SoundByFile" =>
            s"""${stripVarName}$$ = "Sound ${fileName}""""
          case "TextGridByFile" =>
            s"""${stripVarName}$$ = "TextGrid ${fileName}""""

          case _ => "dummmy-- "
        }

      case ValDef(_, TermName(varName), TypeTree(), Apply(Select(Select(This(TypeName(_)), reciever@TermName(recieverName)), TermName(methodName)), params)) =>
        reciever
        s"""|selectObject: ${recieverName}
            |${varName} = ${methodName} ${params.map(typeCheckAndParse).mkString(" ")}
            |""".stripMargin

      case ValDef(Modifiers(_), TermName(varName), typeTree, rightTree) =>

        // variable name, considering string
        // in praat string variable has suffix $
        val varNameConsideringStr = if(typeTree.toString() == "java.lang.String") varName+"$" else varName

        s"""${varNameConsideringStr} = ${typeCheckAndParse(rightTree)}"""


      case Literal(Constant(c)) =>
        c match{
          case ()   => ""
          case true => "yes"
          case false => "no"
          case _ => c.toString
        }

      case Block(codes, returnValue) =>
        (codes ++ List(returnValue)).map(typeCheckAndParse).mkString("\n").split("\n").map("\t" + _).mkString("\n") +"\n"

      case Ident(TermName(varName)) =>
        varName.toString



      case Apply(Select(Apply(Select(Apply(Select(Ident(_), TermName("intWrapper")), List(loopFrom)), TermName("to")), List(Select(This(TypeName(_)), TermName(loopTo)))), TermName("foreach")), List(Function(List(ValDef(Modifiers(_), TermName(counterName), TypeTree(), EmptyTree)), block))) =>
        s"""
           |for ${counterName} from ${loopFrom} to ${loopTo}
           |${parseTree(block)}
           |endfor
           |""".stripMargin


      case exp@Apply(Select(leftHand , op), List(rightHand)) =>

        val opNameOpt = op match {
          case TermName("$minus") => Some("-")
          case TermName("$plus") => Some("+")
          case TermName("$times") => Some("*")
          case _ => None
        }
        opNameOpt match{
          case Some(opName)=>
            s"""(${typeCheckAndParse(leftHand)} ${opName} ${typeCheckAndParse(rightHand)})"""
          case _ => unknownExp(exp)
        }

      //    case If(Literal(Constant(true)), Literal(Constant(())), Literal(Constant(()))) =>


      //    case If(condition, Literal(Constant(())), Literal(Constant(()))) =>
      //
      //      "\n" + condition.toString()
      //
      ////      "\naaa"

      case exp =>
        unknownExp(exp)
    }
  }

  def parseTree(tree: Tree): String = tree match {

    case Block(ModuleDef(_, _, Template(_, _, defaultConstructor :: codes))::_ , _) =>
//      parseTree(head)
      // codeが解析したいコードのリスト
      codes.map(parseTree).mkString("\n") // TODO parseTreesにする
//    case  =>


//      "dummy --- "


    case ValDef(_, TermName(varName), _, Apply(Select(Ident(termName), TermName("apply")), List(Literal(Constant(fileName))))) =>

      termName.toString match {
        case "SoundByFile" =>
          s"""${varName}$$ = "Sound ${fileName}""""
        case "TextGridByFile" =>
          s"""${varName}$$ = "TextGrid ${fileName}""""

        case _ => "dummmy-- "
      }

    case ValDef(_, TermName(varName), TypeTree(), Apply(Select(Select(This(TypeName(_)), TermName(recieverName)), TermName(methodName)), params)) =>
      s"""|selectObject: ${recieverName}
         |${varName} = ${methodName} ${params.map(parseTree).mkString(" ")}
         |""".stripMargin

    case ValDef(Modifiers(_), TermName(varName), TypeTree(), rightTree) =>
      s"""${varName} = ${parseTree(rightTree)}"""


    case Literal(Constant(c)) =>
      c match{
        case ()   => ""
        case true => "yes"
        case false => "no"
        case _ => c.toString
      }

    case Block(codes, returnValue) =>
      (codes ++ List(returnValue)).map(parseTree).mkString("\n").split("\n").map("\t" + _).mkString("\n") +"\n"

    case Ident(TermName(varName)) =>
      varName.toString



    case Apply(Select(Apply(Select(Apply(Select(Ident(_), TermName("intWrapper")), List(loopFrom)), TermName("to")), List(Select(This(TypeName(_)), TermName(loopTo)))), TermName("foreach")), List(Function(List(ValDef(Modifiers(_), TermName(counterName), TypeTree(), EmptyTree)), block))) =>
      s"""
         |for ${counterName} from ${loopFrom} to ${loopTo}
        |${parseTree(block)}
        |endfor
        |""".stripMargin


    case exp@Apply(Select(leftHand , op), List(rightHand)) =>

      println(
        s"""
          |
          |
          |${exp.tpe}
          |
          ${Select(leftHand , op)}
          |
          |
        """.stripMargin)

      val opNameOpt = op match {
        case TermName("$minus") => Some("-")
        case TermName("$plus") => Some("+")
        case TermName("$times") => Some("*")
        case _ => None
      }
      opNameOpt match{
        case Some(opName)=>
          s"""(${parseTree(leftHand)} ${opName} ${parseTree(rightHand)})"""
        case _ => unknownExp(exp)
      }

//    case If(Literal(Constant(true)), Literal(Constant(())), Literal(Constant(()))) =>


//    case If(condition, Literal(Constant(())), Literal(Constant(()))) =>
//
//      "\n" + condition.toString()
//
////      "\naaa"

    case exp =>
      unknownExp(exp)
  }

  def unknownExp(exp: Tree)= s"# Unknown ${showRaw(exp)}\n"
//
//  def parseTrees(trees: List[Tree], topLebelObjects: Map[String, String]): String = trees match {
//    case (head :: rest) =>
//      val oneLine = parseTree(head, topLebelObjects) + "\n" + parseTrees(rest)
//    case Nil            => ""
//  }
}
