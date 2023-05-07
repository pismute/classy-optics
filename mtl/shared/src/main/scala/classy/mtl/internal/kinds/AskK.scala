package classy.mtl.internal.kinds

import cats.{~>, Applicative}
import cats.mtl.Ask

private[classy] open class AskK[F[_], G[_]: Applicative, A](parent: Ask[F, A], fk: F ~> G) extends Ask[G, A]:

  inline def applicative: Applicative[G] = summon

  def ask[A1 >: A]: G[A1] = fk(parent.ask)

end AskK
