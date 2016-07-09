//
////import scalaz./ UnionTypes.Converter
//import scalaz._
///**
//  * Created by Ryo on 2016/07/09.
//  */
//object UnionType {
//  type StringOrInt = String \/ Int
//
//  type ![A] = A => Nothing
//  type !![A] = ![![A]]
//
//  trait Disj { self =>
//    type D
//    type t[S] = Disj {
//      type D = self.D with ![S]
//    }
//  }
//
//  type t[T] = {
//    type t[S] = (Disj { type D = ![T] })#t[S]
//  }
//
//  type or[T <: Disj] = ![T#D]
//
//  type Contains[S, T <: Disj] = !![S] <:< or[T]
//  type ∈[S, T <: Disj] = Contains[S, T]
//
//
//  sealed trait Union[T] {
//    val value: Any
//  }
//
//  case class Converter[S](s: S) {
//    def union[T <: Disj](implicit ev: Contains[S, T]): Union[T] =
//      new Union[T] {
//        val value = s
//      }
//  }
//
//  def size(a: String \/ Int): Int = a match {
//    case \/-(i)  => i
//    case -\/(s) => s.length
//  }
//
//
//
//
//  def main(args: Array[String]) {
//    implicit def any2Converter[S](s: S): Converter[S] = Converter[S](s)
////    println(implicitly[Int ∈ StringOrInt])
//
//    println(size("hello".left[Int]))
//    println("hello")
//  }
//}
