package classy.effect.internal

import cats.Monad
import cats.effect.{IO, IOLocal, LiftIO}
import cats.mtl.Stateful

import classy.optics.Lens

private[classy] class IOLocalStateful[A](parent: IOLocal[A]) extends Stateful[IO, A]:

  val monad: Monad[IO] = summon

  def get: IO[A] = parent.get

  def set(a: A): IO[Unit] = parent.set(a)

  override def modify(f: A => A): IO[Unit] = parent.update(f)

end IOLocalStateful
