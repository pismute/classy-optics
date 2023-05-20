package classy.mtl.internal

import cats.~>
import cats.Eval
import cats.Id
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Raise
import cats.mtl.laws.discipline.*

import classy.mtl.*

class RaiseSpec extends SumBaseSuite with classy.SumData:

  type M[A] = EitherT[Id, Data, A]
  given [F[_]](using Raise[F, Data]): Raise[F, MiniInt] = deriveRaise

  checkAll(
    "Raise",
    RaiseTests[M, MiniInt](summon).raise[MiniInt]
  )

  checkAll(
    "Raise.mapK", {
      type MM[A] = EitherT[Eval, Data, A]
      val fk: ~>[M, MM] = FunctionK.lift([a] => (fa: M[a]) => fa.mapK(idToEvalK))
      given Raise[MM, MiniInt] = summon[Raise[M, MiniInt]].mapK(fk)

      RaiseTests[MM, MiniInt](summon).raise[MiniInt]
    }
  )

  test("contravariant test on a union type") {
    trait DbError

    trait HttpError

    type AppError = DbError | HttpError

    type MM[A] = Either[AppError, A]

    summon[Raise[MM, DbError]]

    summon[Raise[MM, HttpError]]
  }

end RaiseSpec
