package classy.mtl

import cats.Applicative
import cats.Functor
import cats.Monad
import cats.mtl.*

import classy.mtl.internal.*
import classy.optics.Getter
import classy.optics.Lens
import classy.optics.Prism
import classy.optics.Review

trait deriving:

  def deriveAsk[F[_], A <: Product, B: PinInvariant](using parent: Ask[F, A], getter: Getter[A, B]): Ask[F, B] =
    GetterAsk(parent, getter)

  def deriveLocal[F[_], A, B](using parent: Local[F, A], lens: Lens[A, B]): Local[F, B] = LensLocal(parent, lens)

  def deriveStateful[F[_], A, B](using parent: Stateful[F, A], lens: Lens[A, B]): Stateful[F, B] =
    LensStateful(parent, lens)

  def deriveRaise[F[_], A, B: PinInvariant](using parent: Raise[F, A], review: Review[A, B]): Raise[F, B] =
    ReviewRaise(parent, review)

  def deriveHandle[F[_], A, B](using parent: Handle[F, A], prism: Prism[A, B]): Handle[F, B] =
    PrismHandle(parent, prism)

  def deriveTell[F[_], A, B: PinInvariant](using parent: Tell[F, A], review: Review[A, B]): Tell[F, B] =
    ReviewTell(parent, review)

end deriving

object deriving extends deriving
