package classy.mtl.internal.kinds

import cats.{~>, Monad}
import cats.mtl.Stateful
import cats.syntax.functor.*

import classy.optics.Lens

private[classy] class StatefulK[F[_], G[_]: Monad, A](parent: Stateful[F, A], fk: F ~> G) extends Stateful[G, A]:

  inline def monad: Monad[G] = summon

  def get: G[A] = fk(parent.get)

  def set(a: A): G[Unit] = fk(parent.set(a))

end StatefulK
