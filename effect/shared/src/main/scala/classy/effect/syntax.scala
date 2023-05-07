package classy.effect

import cats.{Applicative, Functor, Monad}
import cats.effect.{IO, IOLocal, LiftIO, Ref}
import cats.effect.std.AtomicCell
import cats.mtl.{Ask, Stateful, Tell}

import classy.effect.internal.*
import classy.mtl.*

trait syntax:

  extension [A](parent: IOLocal[A])
    def ask: Ask[IO, A] = IOLocalAsk(parent)

    def askK[F[_]: LiftIO: Applicative]: Ask[F, A] = ask.mapK[F](LiftIO.liftK)

    def stateful: Stateful[IO, A] = IOLocalStateful(parent)

    def statefulK[F[_]: LiftIO: Monad]: Stateful[F, A] = stateful.mapK[F](LiftIO.liftK)

    def tell: Tell[IO, A] = IOLocalTell(parent)

    def tellK[F[_]: LiftIO: Functor]: Tell[F, A] = tell.mapK[F](LiftIO.liftK)

  extension [F[_], A](parent: Ref[F, A])
    def ask(using Applicative[F]): Ask[F, A] = RefAsk(parent)

    def tell(using Functor[F]): Tell[F, A] = RefTell(parent)

  extension [F[_], A](parent: AtomicCell[F, A])
    def ask(using Applicative[F]): Ask[F, A] = AtomicCellAsk(parent)

    def stateful(using Monad[F]): Stateful[F, A] = AtomicCellStateful(parent)

    def tell(using Functor[F]): Tell[F, A] = AtomicCellTell(parent)

end syntax

object syntax extends syntax
