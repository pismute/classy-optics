package classy.effect.internal

import cats.Applicative
import cats.effect.IO
import cats.effect.IOLocal
import cats.mtl.Ask

private[classy] class IOLocalAsk[A](parent: IOLocal[A]) extends Ask[IO, A]:

  def applicative: Applicative[IO] = summon

  def ask[A1 >: A]: IO[A1] = parent.get

end IOLocalAsk
