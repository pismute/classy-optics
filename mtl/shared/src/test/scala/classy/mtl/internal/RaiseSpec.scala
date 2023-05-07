package classy.mtl.internal

import cats.{~>, Id}
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Raise
import cats.mtl.laws.discipline.*

import classy.mtl.*

class RaiseSpec extends SumBaseSuite with classy.SumData:

  type M[A] = Either[Data, A]
  given [F[_]](using Raise[F, Data]): Raise[F, MiniInt] = deriveRaise

  checkAll(
    "Raise",
    RaiseTests[M, MiniInt](summon).raise[MiniInt]
  )

  checkAll(
    "Raise.mapK", {
      type F[A] = EitherT[Id, Data, A]
      val fk: ~>[M, F] = FunctionK.lift([a] => (ma: M[a]) => EitherT.fromEither[Id](ma))
      given Raise[F, MiniInt] = summon[Raise[M, MiniInt]].mapK(fk)

      RaiseTests[F, MiniInt](summon).raise[MiniInt]
    }
  )

end RaiseSpec
