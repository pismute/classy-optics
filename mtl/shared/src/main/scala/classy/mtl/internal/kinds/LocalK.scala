package classy.mtl.internal.kinds

import cats.{~>, Applicative}
import cats.mtl.Local

private[classy] class LocalK[F[_], G[_]: Applicative, A](parent: Local[F, A], fk: F ~> G, gk: G ~> F)
    extends AskK[F, G, A](parent, fk)
    with Local[G, A]:

  def local[C](gc: G[C])(f: A => A): G[C] = fk(parent.local(gk(gc))(f))

end LocalK
