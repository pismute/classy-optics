package classy.effect.internal

import cats.Functor
import cats.effect.std.AtomicCell
import cats.mtl.Tell

private[classy] class AtomicCellTell[F[_]: Functor, A](parent: AtomicCell[F, A]) extends Tell[F, A]:

  def functor: Functor[F] = summon

  def tell(a: A): F[Unit] = parent.set(a)

end AtomicCellTell
