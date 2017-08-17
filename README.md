# Typed Praat

Praat transpiler written in Scala

### What is this?

```
[Scala DSL for Praat] ==transpile=> [Praat script]  
```

A Scala code bellow will be transpiled into praat script

```scala
import praat._

object Assignment4 {


  fileAppend("Data2.txt", "Vowel\tDuration(s)\tF0 at midpoint(s)(Hz)\tMax Intensity(dB)\n")

  val soundAssinment4 = SoundByFile("Assignment4")
  val textGrid = TextGridByFile("Assignment4")
  val pitch = soundAssinment4.toPitch(0.0, 75.0, 600.0)
  val numberOfLabels = textGrid.getNumberOfIntervals(1)
  val intensity = soundAssinment4.toIntensity(75.0, 0.0, subtractMean = true)

  for (i <- 1 to numberOfLabels) {
    val label = textGrid.getLabelOfInterval(1, i)
    if (label != "") {
      val start = textGrid.getStartingPoint(1, i)
      val end = textGrid.getEndPoint(1, i)
      val mid = 0.5 * (start + end)
      val meanf = pitch.getValueAtTime(mid, Hertz, Linear)
      val maxIntensity = intensity.getMaximum(start, end, Parabolic)
      val duration = end - start

      fileAppend("Data2.txt", s"${label}\t${duration}\t${meanf}\t${maxIntensity}\n")
    }
  }

}
```


Here is a praat script transpiled

```praat

fileappend Data2.txt Vowel'tab$'Duration(s)'tab$'F0 at midpoint(s)(Hz)'tab$'Max Intensity(dB)'newline$'
soundAssinment4$ = "Sound Assignment4"

textGrid$ = "TextGrid Assignment4"

selectObject: soundAssinment4$
pitch = To Pitch... 0.0 75.0 600.0


selectObject: textGrid$
numberOfLabels = Get number of intervals... 1


selectObject: soundAssinment4$
intensity = To Intensity... 75.0 0.0 yes



for i from 1 to numberOfLabels
	selectObject: textGrid$
	label$ = Get label of interval... 1 i
	
	if (label$ <> "")
		selectObject: textGrid$
		start = Get starting point... 1 i
		
		selectObject: textGrid$
		end = Get end point... 1 i
		
		mid = (0.5 * (start + end))
		selectObject: pitch
		meanf = Get value at time... mid Hertz Linear
		
		selectObject: intensity
		maxIntensity = Get maximum... start end Parabolic
		
		duration = (end - start)
		fileappend Data2.txt 'label$''tab$''duration''tab$''meanf''tab$''maxIntensity''newline$'
	
	else
	
	endif
	         

endfor

```
