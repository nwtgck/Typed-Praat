package praat

import Types._
import scalaz._

/**
  * Created by Jimmy on 2016/07/08.
  */
case class Pitch(timeStep: Real, pitchFloor: Real, pitchCeilling: Real) {
  def getValueAtTime(timeRange: Real, unit: Unit, interpolation: Interpolation): Real = Dummys.real
  def getMaximum(timeRange: Real, unit: Unit, interpolation: Interpolation): Real = Dummys.real
}
