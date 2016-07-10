package praat

import Types._
import praat.annotation.PraatName

/**
  * Created by Jimmy on 2016/07/08.
  */
case class Sound(){
  @PraatName(name="To Pitch")
  def toPitch(timeStep : Real, pitchFloor: Real, pitchCeilling: Real) = Pitch(timeStep, pitchFloor, pitchCeilling)

  @PraatName(name="To Intensity")
  def toIntensity(maximumPitch: Real, timeStep: Real, subtractMean: Boolean) = Intensity(maximumPitch, timeStep, subtractMean)
}


