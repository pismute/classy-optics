package classy.effect.internal

import cats.Applicative
import cats.effect.Ref
import cats.mtl.Ask

private[classy] class RefAsk[F[_]: Applicative, A](parent: Ref[F, A]) extends Ask[F, A]:

  def applicative: Applicative[F] = summon

  def ask[A1 >: A]: F[A1] = applicative.widen(parent.get)

end RefAsk
