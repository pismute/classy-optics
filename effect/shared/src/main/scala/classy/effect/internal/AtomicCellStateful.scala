package classy.effect.internal

import cats.Monad
import cats.effect.std.AtomicCell
import cats.mtl.Stateful

import classy.optics.Lens

private[classy] class AtomicCellStateful[F[_]: Monad, A](parent: AtomicCell[F, A]) extends Stateful[F, A]:

  val monad: Monad[F] = summon

  def get: F[A] = parent.get

  def set(a: A): F[Unit] = parent.set(a)

  override def modify(f: A => A): F[Unit] = parent.update(f)

end AtomicCellStateful
