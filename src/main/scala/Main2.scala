import scala.reflect.runtime.universe._

/**
  * Created by Jimmy on 2016/07/08.
  */
object Main2 {
  def main(args: Array[String]) {

    import praat._

    val exp = reify {
      object Assignment4 {

        fileAppend("Data2.txt", "Vowel\tDuration(s)\tF0 at midpoint(s)(Hz)\tMax Intensity(dB)\n")

        val soundAssinment4 = SoundByFile("Assignment4")
        val textGrid = TextGridByFile("Assignment4")
        val pitch = soundAssinment4.toPitch(0.0, 75.0, 600.0)
        val numberOfLabels = textGrid.getNumberOfIntervals(1)
        val intensity = soundAssinment4.toIntensity(75.0, 0.0, true)

        for (i <- 1 to numberOfLabels) {
          val label = textGrid.getLabelOfInterval(1, i)
          if (label != "") {
            val start = textGrid.getStartingPoint(1, i)
            val end = textGrid.getEndPoint(1, i)
            val mid = 0.5 * (start + end)
            val meanf = pitch.getValueAtTime(mid, Hertz, Linear)
            val maxIntensity = intensity.getMaximum(start, end, Hertz, Parabolic)
            val duration = end - start

            fileAppend("Data2.txt", s"${label}\t${duration}\t${meanf}\t${maxIntensity}\n")
          }
        }

      }
    }


    val converter = new ToPraatConverter(exp.tree)
    println(converter.praatScript)

  }
}
