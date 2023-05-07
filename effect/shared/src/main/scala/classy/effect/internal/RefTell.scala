package classy.effect.internal

import cats.Functor
import cats.effect.Ref
import cats.mtl.Tell

private[classy] class RefTell[F[_]: Functor, A](parent: Ref[F, A]) extends Tell[F, A]:

  def functor: Functor[F] = summon

  def tell(a: A): F[Unit] = parent.set(a)

end RefTell
