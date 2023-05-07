package classy.mtl

import scala.util.{Failure, Success, Try}

import cats.{~>, Applicative, Functor, Monad}
import cats.data.Validated
import cats.mtl.*

import classy.mtl.internal.kinds.*

trait syntax:

  extension [A](oa: Option[A])
    def liftTo[F[_], B](ifEmpty: => B)(using F: Applicative[F], R: Raise[F, ? >: B]): F[A] =
      oa match {
        case Some(a) => F.pure(a)
        case None    => R.raise(ifEmpty)
      }

  extension [A, B](eab: Either[A, B])
    def liftTo[F[_]](using F: Applicative[F], R: Raise[F, ? >: A]): F[B] =
      eab match {
        case Left(a)  => R.raise(a)
        case Right(b) => F.pure(b)
      }

  extension [A](ta: Try[A])
    def liftTo[F[_]](using F: Applicative[F], R: Raise[F, Throwable]): F[A] =
      ta match {
        case Failure(e) => R.raise(e)
        case Success(a) => F.pure(a)
      }

  extension [A, B](veab: Validated[A, B])
    def liftTo[F[_]](using F: Applicative[F], R: Raise[F, ? >: A]): F[B] =
      veab match {
        case Validated.Invalid(a) => R.raise(a)
        case Validated.Valid(b)   => F.pure(b)
      }

  extension [F[_], A](parent: Ask[F, A]) def mapK[G[_]: Applicative](fk: F ~> G): Ask[G, A] = AskK(parent, fk)
  extension [F[_], A](parent: Local[F, A])
    def imapK[G[_]: Applicative](fk: F ~> G, gk: G ~> F): Local[G, A] = LocalK(parent, fk, gk)
  extension [F[_], A](parent: Stateful[F, A]) def mapK[G[_]: Monad](fk: F ~> G): Stateful[G, A] = StatefulK(parent, fk)
  extension [F[_], A](parent: Raise[F, A]) def mapK[G[_]: Functor](fk: F ~> G): Raise[G, A] = RaiseK(parent, fk)
  extension [F[_], A](parent: Handle[F, A])
    def imapK[G[_]: Applicative](fk: F ~> G, gk: G ~> F): Handle[G, A] = HandleK(parent, fk, gk)
  extension [F[_], A](parent: Tell[F, A]) def mapK[G[_]: Functor](fk: F ~> G): Tell[G, A] = TellK(parent, fk)

end syntax

object syntax extends syntax
