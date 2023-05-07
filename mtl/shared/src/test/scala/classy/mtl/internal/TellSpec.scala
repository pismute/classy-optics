package classy.mtl.internal

import cats.{~>, Functor, Id}
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Tell
import cats.mtl.laws.discipline.*

import classy.mtl.*

class TellSpec extends SumBaseSuite with classy.SumData:

  type M[A] = Either[Data, A]
  given [F[_]](using Tell[F, Data]): Tell[F, MiniInt] = deriveTell

  given Tell[M, Data] with
    def functor: Functor[M] = summon

    def tell(l: Data): M[Unit] = Right(())

  checkAll(
    "Tell",
    TellTests[M, MiniInt](summon).tell[MiniInt]
  )

  checkAll(
    "Tell.mapK", {
      type F[A] = EitherT[Id, Data, A]
      val fk: ~>[M, F] = FunctionK.lift([a] => (ma: M[a]) => EitherT.fromEither[Id](ma))
      given Tell[F, MiniInt] = summon[Tell[M, MiniInt]].mapK(fk)

      TellTests[F, MiniInt](summon).tell[MiniInt]
    }
  )
end TellSpec
