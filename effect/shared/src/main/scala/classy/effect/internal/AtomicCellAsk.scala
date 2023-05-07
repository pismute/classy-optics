package classy.effect.internal

import cats.Applicative
import cats.effect.std.AtomicCell
import cats.mtl.Ask

private[classy] class AtomicCellAsk[F[_]: Applicative, A](parent: AtomicCell[F, A]) extends Ask[F, A]:

  def applicative: Applicative[F] = summon

  def ask[A1 >: A]: F[A1] = applicative.widen(parent.get)

end AtomicCellAsk
