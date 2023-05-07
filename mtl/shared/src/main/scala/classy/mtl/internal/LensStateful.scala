package classy.mtl.internal

import cats.Monad
import cats.mtl.Stateful
import cats.syntax.functor.*

import classy.optics.Lens

private[classy] class LensStateful[F[_], A, B](parent: Stateful[F, A], lens: Lens[A, B]) extends Stateful[F, B]:

  inline def monad: Monad[F] = parent.monad

  def get: F[B] = monad.map(parent.get)(lens.view)

  def set(b: B): F[Unit] = parent.modify(a => lens.set(a)(b))

end LensStateful
