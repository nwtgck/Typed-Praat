import praat.annotation.PraatName

/**
  * Created by Ryo on 2016/07/09.
  */
package object praat {
  // Top level functions

  // fileappendで「""」の引用符なしでかけるものに使う
  // 変数を使いたいときは「''」で囲む
  private[praat] case class RawString(string: String)

  implicit def string2RawString = RawString


  @PraatName(name="fileappend")
  def fileAppend(fileName: RawString, content: RawString): scala.Unit = ()

  @PraatName(name="echo")
  def echo(rawString: RawString): scala.Unit = ()

  @PraatName(name="printline")
  def printLine(rawString: RawString): scala.Unit = ()

  //
//  // tab
//  val tab$ = "\t"
//  // new line
//  val newline$ = "\n"

}
