package classy

import scala.util.NotGiven

import cats.Functor
import cats.effect.Ref

import classy.optics.Lens

// package object is deprecated, but export on packages has a bug
// https://github.com/lampepfl/dotty/issues/17201
package object effect extends deriving with syntax:

  object auto:

    given [F[_]: Functor, A, B](using ev: NotGiven[Ref[F, B]], parent: Ref[F, A], lens: Lens[A, B]): Ref[F, B] =
      deriveRef

  end auto

end effect
