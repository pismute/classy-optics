package classy.mtl.internal

import cats.~>
import cats.Eq
import cats.FlatMap
import cats.Id
import cats.arrow.FunctionK
import cats.data.Kleisli
import cats.data.StateT

import org.scalacheck.*

trait ProductBaseSuite extends classy.BaseSuite:
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(
      Gen.oneOf(
        new (Option ~> Option) {
          def apply[A](fa: Option[A]): Option[A] = None
        },
        FunctionK.id[Option]
      )
    )

  given eqKleisli[F[_], A, B](using Arbitrary[A], Eq[A => F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: Kleisli[F, A, B]) => x.run)

  given stateTEq[F[_], S, A](using Arbitrary[S], Eq[S => F[(S, A)]], FlatMap[F]): Eq[StateT[F, S, A]] =
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state => s => state.run(s))

  def eqFAB[F[_], A, B](using a: Arbitrary[A], eq: Eq[F[B]]): Eq[A => F[B]] = Eq.instance { (f, g) =>
    val av: A = a.arbitrary.sample.get
    eq.eqv(f(av), g(av))
  }

  given [A: Arbitrary, B: Eq]: Eq[A => B] = eqFAB[Id, A, B]

end ProductBaseSuite
