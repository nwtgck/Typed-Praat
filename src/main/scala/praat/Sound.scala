package praat

import Types._

/**
  * Created by Jimmy on 2016/07/08.
  */
case class Sound(){
  def toPitch(timeStep : Real, pitchFloor: Real, pitchCeilling: Real) = Pitch(timeStep, pitchFloor, pitchCeilling)
  def toIntensity(maximumPitch: Real, timeStep: Real, subtractMean: Boolean) = Intensity(maximumPitch, timeStep, subtractMean)
}


