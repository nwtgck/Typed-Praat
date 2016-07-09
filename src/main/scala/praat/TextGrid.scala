package praat

import praat.Types._

/**
  * Created by Jimmy on 2016/07/08.
  */
case class TextGrid(){
  def getNumberOfIntervals(tierNumber: Whole): Whole = Dummys.wholeNum
  def getLabelOfInterval(tierNumber: Whole, intervalNumber: Whole): String = Dummys.string
  def getStartingPoint(a: Whole, b: Whole): Real = Dummys.real
  def getEndPoint(a: Whole, b: Whole): Real = Dummys.real
}
