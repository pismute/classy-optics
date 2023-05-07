package classy.effect

import cats.Functor
import cats.effect.Ref
import cats.effect.std.AtomicCell

import classy.effect.internal
import classy.optics.Lens

trait deriving:

  /** `cats.mtl.Stateful` can not be derived from `Ref` because it is not
    * atomic(https://github.com/typelevel/cats-mtl/pull/1200). Instead, we can use `Ref` like mtl typeclasses. `Ref` has
    * the shape of `MTL[_[_], _]`, it has a good fit to work
    *
    * {{{
    *   case class MyCache(httpCache: HttpCache, dbCache: DbCache)
    *
    *   type AppT[A] = EitherT[ReaderT[IO, AppEnv, *], AppError, A]
    *   given Ref[AppT, MyCache] = ...
    *   given Ref[AppT, HttpCache] = deriveRef // Automatically derived via a lens
    * }}}
    */
  inline def deriveRef[F[_]: Functor, A, B](using parent: Ref[F, A], lens: Lens[A, B]): Ref[F, B] =
    Ref.lens[F, A, B](parent)(lens.view, lens.set)

end deriving

object deriving extends deriving
