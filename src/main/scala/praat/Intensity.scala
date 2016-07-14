package praat

import Types._
import praat.annotation.PraatName
import unionType.UnionTypes._

/**
  * Created by Jimmy on 2016/07/08.
  */
case class Intensity(maximumPitch: Real, timeStep: Real, subtractMean: Boolean) {
// Interpolationを選べないものを限定する型定義（完全に対応できないのでコメントアウト）
  //  @PraatName(name="Get maximum")
//  def getMaximum[Intr](start: Real, end: Real, interpolation: Intr)
//                      (implicit c: Intr ∈ t[Noneable]#t[Prabolicable]): Real = Dummys.real

  @PraatName(name="Get maximum")
  def getMaximum(start: Real, end: Real, interpolation: Interpolation): Real = Dummys.real

  @PraatName(name="Get start time")
  def getStartTime(): Real = Dummys.real

  @PraatName(name="Get end time")
  def getEndTime(): Real = Dummys.real

  @PraatName(name="Get total duration")
  def getTotalDuration(): Real = Dummys.real


  @PraatName(name="Get number of frames")
  def getNumberOfFrames(): Whole = Dummys.wholeNum

  @PraatName(name="Get time step")
  def getTimeStep(): Real = Dummys.real

  @PraatName(name="Get time from frame number")
  def getTimeFromFrameNumber(a: Whole): Real = Dummys.real

  @PraatName(name="Get frame number from time")
  def getFrameNumberFromTime(a: Real): Real = Dummys.real

//  def getNumber

}
