package praat

import Types._

/**
  * Created by Jimmy on 2016/07/08.
  */
case class Intensity(maximumPitch: Real, timeStep: Real, subtractMean: Boolean) {
  def getMaxmum(start: Real, end: Real, interpolation: Interpolation): Real = Dummys.real
}
