package classy.mtl.internal.kinds

import cats.~>
import cats.Functor
import cats.mtl.Raise

private[classy] open class RaiseK[F[_], G[_]: Functor, A](parent: Raise[F, A], fk: F ~> G) extends Raise[G, A]:

  def functor: Functor[G] = summon

  def raise[A1 <: A, C](a: A1): G[C] = fk(parent.raise(a))

end RaiseK
