package praat

/**
  * Created by Jimmy on 2016/07/08.
  */
sealed abstract class Interpolation
case object None extends Interpolation with Noneable
case object Parabolic extends Interpolation with Prabolicable
case object Linear extends Interpolation with Linearable
case object Cubic extends Interpolation with Cubicable
case object Sinc70 extends Interpolation with Sinc70able
case object Sinc700 extends Interpolation with Sinc70able

sealed trait Noneable
sealed trait Prabolicable
sealed trait Linearable
sealed trait Cubicable
sealed trait Sinc70able
sealed trait Sinc700able