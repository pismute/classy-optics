package classy.mtl

import scala.util.{Failure, Success, Try}

import cats.{Applicative, Functor, Monad}
import cats.mtl.*
import cats.mtl.implicits.*

import classy.mtl.auto.given

class AutoSpec extends classy.BaseSuite:
  type Sum = Either[Throwable, Int]
  type M[A] = Either[Sum, A]

  given Tell[M, Sum] with
    def functor: Functor[M] = summon

    def tell(l: Sum): M[Unit] = Right(())

  summon[Handle[M, Sum]]
  summon[Raise[M, Sum]]

  summon[Handle[M, Throwable]]
  summon[Handle[M, Left[Throwable, Int]]]
  summon[Handle[M, Right[Throwable, Int]]]
  summon[Handle[M, Int]]

  summon[Raise[M, Throwable]]
  summon[Raise[M, Left[Throwable, Int]]]
  summon[Raise[M, Right[Throwable, Int]]]
  summon[Raise[M, Int]]

  summon[Tell[M, Throwable]]
  summon[Tell[M, Left[Throwable, Int]]]
  summon[Tell[M, Right[Throwable, Int]]]
  summon[Tell[M, Int]]

  case class Product(i: Int, b: Boolean)
  type F[A] = Either[Product, A]

  given Local[F, Product] with
    def applicative: Applicative[F] = summon
    def ask[Product]: F[Product] = Left(Product(1, true))
    def local[A](fa: F[A])(f: Product => Product): F[A] = fa

  given Stateful[F, Product] with
    def monad: Monad[F] = summon
    def get: F[Product] = Right(Product(1, true))
    def set(s: Product): F[Unit] = Right(())

  summon[Ask[F, Product]]
  summon[Local[F, Product]]
  summon[Stateful[F, Product]]

  summon[Ask[F, Int]]
  summon[Ask[F, Boolean]]

  summon[Local[F, Int]]
  summon[Local[F, Boolean]]

  summon[Stateful[F, Int]]
  summon[Stateful[F, Boolean]]

end AutoSpec
