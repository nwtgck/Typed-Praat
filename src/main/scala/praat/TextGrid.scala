package praat

import praat.Types._
import praat.annotation.PraatName

/**
  * Created by Jimmy on 2016/07/08.
  */
case class TextGrid(){
  @PraatName(name="Get number of intervals")
  def getNumberOfIntervals(tierNumber: Whole): Whole = Dummys.wholeNum

  @PraatName(name="Get label of interval")
  def getLabelOfInterval(tierNumber: Whole, intervalNumber: Whole): String = Dummys.string

  @PraatName(name="Get starting point")
  def getStartingPoint(a: Whole, b: Whole): Real = Dummys.real

  @PraatName(name="Get end point")
  def getEndPoint(a: Whole, b: Whole): Real = Dummys.real
}
