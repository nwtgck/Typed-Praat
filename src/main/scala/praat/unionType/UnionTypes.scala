package praat.unionType

/**
  * Created by Ryo on 2016/07/10.
  */
trait UnionTypes {
  type ![A] = A => Nothing
  type !![A] = ![![A]]

  trait Disj { self =>
    type D
    type t[S] = Disj {
      type D = self.D with ![S]
    }
  }

  type t[T] = {
    type t[S] = (Disj { type D = ![T] })#t[S]
  }

  type or[T <: Disj] = ![T#D]

  type Contains[S, T <: Disj] = !![S] <:< or[T]
  type ∈[S, T <: Disj] = Contains[S, T]

  sealed trait Union[T] {
    val value: Any
  }
}

object UnionTypes extends UnionTypes