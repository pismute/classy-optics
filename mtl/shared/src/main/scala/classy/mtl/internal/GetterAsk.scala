package classy.mtl.internal

import cats.Applicative
import cats.mtl.Ask

import classy.optics.Getter

private[classy] open class GetterAsk[F[_], A, B](parent: Ask[F, A], getter: Getter[A, B]) extends Ask[F, B]:

  inline def applicative: Applicative[F] = parent.applicative

  def ask[B1 >: B]: F[B1] = applicative.map(parent.ask)(getter.view)

end GetterAsk
