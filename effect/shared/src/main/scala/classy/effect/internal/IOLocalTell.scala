package classy.effect.internal

import cats.Functor
import cats.effect.IO
import cats.effect.IOLocal
import cats.mtl.Tell

private[classy] class IOLocalTell[A](parent: IOLocal[A]) extends Tell[IO, A]:

  def functor: Functor[IO] = summon

  def tell(a: A): IO[Unit] = parent.set(a)

end IOLocalTell
