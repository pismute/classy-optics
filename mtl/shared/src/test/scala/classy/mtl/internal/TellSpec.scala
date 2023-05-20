package classy.mtl.internal

import cats.~>
import cats.Eval
import cats.Functor
import cats.Id
import cats.arrow.FunctionK
import cats.data.State
import cats.data.WriterT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Tell
import cats.mtl.laws.discipline.*

import classy.mtl.*

class TellSpec extends SumBaseSuite with classy.SumData:

  type M[A] = WriterT[Id, Data, A]
  given [F[_]](using Tell[F, Data]): Tell[F, MiniInt] = deriveTell

  checkAll(
    "Tell",
    TellTests[M, MiniInt](summon).tell[MiniInt]
  )

  checkAll(
    "Tell.mapK", {
      type MM[A] = WriterT[Eval, Data, A]
      val fk: ~>[M, MM] = FunctionK.lift([a] => (ma: M[a]) => ma.mapK(idToEvalK))
      given Tell[MM, MiniInt] = summon[Tell[M, MiniInt]].mapK(fk)

      TellTests[MM, MiniInt](summon).tell[MiniInt]
    }
  )

  test("contravariant test on a union type") {
    trait DbError

    trait HttpError

    type AppError = DbError | HttpError

    type MM[A] = State[AppError, A]

    given Tell[MM, AppError] with
      def functor: Functor[MM] = summon

      def tell(l: AppError): MM[Unit] = State.set(l)

    summon[Tell[MM, DbError]]

    summon[Tell[MM, HttpError]]
  }

end TellSpec
