package classy.mtl.internal.kinds

import cats.~>
import cats.Functor
import cats.mtl.Tell

private[classy] class TellK[F[_], G[_]: Functor, A](parent: Tell[F, A], fk: F ~> G) extends Tell[G, A]:

  inline def functor: Functor[G] = summon

  def tell(a: A): G[Unit] = fk(parent.tell(a))

end TellK
