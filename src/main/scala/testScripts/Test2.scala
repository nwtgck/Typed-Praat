package testScripts

import praat._

/**
  * Created by Ryo on 2016/07/10.
  */
object Test2 {

  val intensity = IntensityByFile("untitled")

  val startTime = intensity.getStartTime()
  val endTime = intensity.getEndTime()
  val totalDur = intensity.getTotalDuration()
  val frames = intensity.getNumberOfFrames()
  val timeStep = intensity.getTimeStep()
  val frameNum = intensity.getTimeFromFrameNumber(1)
  val frameNumberFromTime = intensity.getFrameNumberFromTime(0.5)



  echo("A")
  printLine(s"$startTime, $endTime $totalDur $frames $timeStep $frameNum $frameNumberFromTime")

}
