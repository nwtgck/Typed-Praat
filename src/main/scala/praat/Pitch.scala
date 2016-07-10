package praat

import Types._
import praat.annotation.PraatName
import unionType.UnionTypes._

/**
  * Created by Jimmy on 2016/07/08.
  */


case class Pitch(timeStep: Real, pitchFloor: Real, pitchCeilling: Real) {
  @PraatName(name="Get value at time")
  def getValueAtTime[Intr](timeRange: Real, unit: Unit, interpolation: Intr)(implicit c: Intr ∈ t[Noneable]#t[Linearable]): Real = Dummys.real

  @PraatName(name="Get maximum")
  def getMaximum[Intr](timeRange: Real, unit: Unit, interpolation: Intr)(implicit c: Intr ∈ t[Noneable]#t[Prabolicable]): Real = Dummys.real
}
