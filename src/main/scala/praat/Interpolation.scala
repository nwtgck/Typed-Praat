package praat

/**
  * Created by Jimmy on 2016/07/08.
  */
sealed abstract class Interpolation
case object NoneInterpolation extends Interpolation
case object Parabolic extends Interpolation
case object Linear extends Interpolation
