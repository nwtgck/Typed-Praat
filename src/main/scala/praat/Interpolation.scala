package praat

/**
  * Created by Jimmy on 2016/07/08.
  */
sealed abstract class Interpolation
case object None extends Interpolation with Noneable
case object Parabolic extends Interpolation with Prabolicable
case object Linear extends Interpolation with Linearable

sealed trait Noneable
sealed trait Prabolicable
sealed trait Linearable