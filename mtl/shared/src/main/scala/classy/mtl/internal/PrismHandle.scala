package classy.mtl.internal

import cats.Applicative
import cats.mtl.Handle

import classy.optics.Prism

private[classy] class PrismHandle[F[_], A <: Matchable, B](parent: Handle[F, A], prism: Prism[A, B])
    extends ReviewRaise[F, A, B](parent, prism)
    with Handle[F, B]:

  inline def applicative: Applicative[F] = parent.applicative

  def handleWith[C](fa: F[C])(f: B => F[C]): F[C] = parent.handleWith(fa) {
    case prism(b) => f(b)
    case a        => parent.raise(a)
  }

end PrismHandle
