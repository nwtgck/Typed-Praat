package praat

import Types._
import unionType.UnionTypes._

/**
  * Created by Jimmy on 2016/07/08.
  */
case class Intensity(maximumPitch: Real, timeStep: Real, subtractMean: Boolean) {
  def getMaximum[Intr](start: Real, end: Real, unit: Unit, interpolation: Intr)(implicit c: Intr ∈ t[Prabolicable]#t[Noneable]): Real = Dummys.real
}
