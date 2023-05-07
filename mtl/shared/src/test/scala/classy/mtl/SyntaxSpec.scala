package classy.mtl

import scala.util.{Failure, Success, Try}

import cats.Functor
import cats.data.Validated
import cats.mtl.*
import cats.syntax.either.*

class SyntaxSpec extends classy.BaseSuite:
  type Data = Either[Throwable, Int]
  type M[A] = Either[Data, A]
  given Raise[M, Data] with
    def functor: Functor[M] = summon

    def raise[E2 <: Data, A](e: E2): M[A] = Left(e)
  given Raise[M, Throwable] = deriveRaise[M, Data, Throwable]
  given Raise[M, Int] = deriveRaise[M, Data, Int]

  val error = RuntimeException("error")
  test("lift Option to either with raise") {
    assertEquals(Option.empty[Int].liftTo[M, Data](error.asLeft[Int]), Left(Left(error)))
    assertEquals(Some(1).liftTo[M, Data](error.asLeft[Int]), 1.asRight[Data])
  }

  test("lift Either to either with raise") {
    assertEquals(error.asLeft[Int].liftTo[M], Left(Left(error)))
    assertEquals(1.asRight[Throwable].liftTo[M], 1.asRight[Data])
  }

  test("lift Try to either with raise") {
    assertEquals(Failure(error).liftTo[M], Left(Left(error)))
    assertEquals(Success(1).liftTo[M], 1.asRight[Data])
  }

  test("lift Validated to either with raise") {
    assertEquals(Validated.invalid[Throwable, Int](error).liftTo[M], Left(Left(error)))
    assertEquals(Validated.valid[Throwable, Int](1).liftTo[M], 1.asRight[Data])
  }

end SyntaxSpec
