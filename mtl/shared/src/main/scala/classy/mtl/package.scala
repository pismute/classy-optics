package classy

import scala.util.NotGiven

import cats.mtl.Ask
import cats.mtl.Handle
import cats.mtl.Local
import cats.mtl.Raise
import cats.mtl.Stateful
import cats.mtl.Tell

import classy.optics.Getter
import classy.optics.Lens
import classy.optics.Prism
import classy.optics.Review

// package object is deprecated, but export on packages has a bug
// https://github.com/lampepfl/dotty/issues/17201
package object mtl extends deriving with syntax:
  /** `Ask[F[_], +E]` has covariant type parameter `E`, so `scalac` search a implicit instance with `Nothing` like
    * `Ask[F[_], Nothing]`. It can be avoided with a specific invariant type class. Coincidently, `Typeable` is good fit
    * to it
    *
    * `Tell[F[_], -L]` and `Raise[F[_], -E]` has the same issue. It tries to pick `Any`.
    */
  type PinInvariant[A] = scala.reflect.Typeable[A]

  object auto:

    given [F[_], A <: Product, B: PinInvariant](using
        NotGiven[Ask[F, B]],
        Ask[F, A],
        Getter[A, B]
    ): Ask[F, B] = deriveAsk

    given [F[_], A, B](using NotGiven[Local[F, B]], Local[F, A], Lens[A, B]): Local[F, B] =
      deriveLocal

    given [F[_], A, B](using NotGiven[Stateful[F, B]], Stateful[F, A], Lens[A, B]): Stateful[F, B] =
      deriveStateful

    given [F[_], A, B: PinInvariant](using
        NotGiven[Raise[F, B]],
        Raise[F, A],
        Review[A, B]
    ): Raise[F, B] =
      deriveRaise

    given [F[_], A, B](using
        NotGiven[Handle[F, B]],
        Handle[F, A],
        Prism[A, B]
    ): Handle[F, B] = deriveHandle

    given [F[_], A, B: PinInvariant](using
        NotGiven[Tell[F, B]],
        Tell[F, A],
        Review[A, B]
    ): Tell[F, B] = deriveTell

  end auto

end mtl
