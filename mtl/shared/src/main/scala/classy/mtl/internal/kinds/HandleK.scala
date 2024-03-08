package classy.mtl.internal.kinds

import cats.~>
import cats.Applicative
import cats.mtl.Handle

private[classy] class HandleK[F[_], G[_]: Applicative, A](parent: Handle[F, A], fk: F ~> G, gk: G ~> F)
    extends RaiseK[F, G, A](parent, fk)
    with Handle[G, A]:

  inline def applicative: Applicative[G] = summon

  def handleWith[C](gc: G[C])(f: A => G[C]): G[C] = fk(parent.handleWith(gk(gc))(a => gk(f(a))))

end HandleK
