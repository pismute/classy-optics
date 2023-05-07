package classy.mtl.internal

import cats.{~>, Id}
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Handle
import cats.mtl.laws.discipline.*

import classy.BaseSuite
import classy.mtl.*

class HandleSpec extends SumBaseSuite with classy.SumData:

  type M[A] = Either[Data, A]
  given [F[_]](using Handle[F, Data]): Handle[F, MiniInt] = deriveHandle

  checkAll(
    "Handle",
    HandleTests[M, MiniInt](summon).handle[MiniInt]
  )

  checkAll(
    "Handle.imapK", {
      type F[A] = EitherT[Id, Data, A]
      val fk: ~>[M, F] = FunctionK.lift([a] => (ma: M[a]) => EitherT.fromEither[Id](ma))
      val gk: ~>[F, M] = FunctionK.lift([a] => (fa: F[a]) => fa.value)
      given Handle[F, MiniInt] = summon[Handle[M, MiniInt]].imapK(fk, gk)

      HandleTests[F, MiniInt](summon).handle[MiniInt]
    }
  )

end HandleSpec
