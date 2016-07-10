/**
  * Created by Ryo on 2016/07/09.
  */
package object praat {
  // Top level functions

  // fileappendで「""」の引用符なしでかけるものに使う
  // 変数を使いたいときは「''」で囲む
  private[praat] case class RawString(string: String)

  implicit def string2RawString = RawString


  // fileappend
  def fileAppend(fileName: RawString, content: RawString): scala.Unit = ()

  //
//  // tab
//  val tab$ = "\t"
//  // new line
//  val newline$ = "\n"

}
