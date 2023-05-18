package classy.mtl.internal

import cats.Eval
import cats.Id
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Handle
import cats.mtl.laws.discipline.*
import cats.~>
import classy.BaseSuite
import classy.mtl.*

class HandleSpec extends SumBaseSuite with classy.SumData:

  type M[A] = EitherT[Id, Data, A]
  given [F[_]](using Handle[F, Data]): Handle[F, MiniInt] = deriveHandle

  checkAll(
    "Handle",
    HandleTests[M, MiniInt](summon).handle[MiniInt]
  )

  checkAll(
    "Handle.imapK", {
      type MM[A] = EitherT[Eval, Data, A]
      val fk: ~>[M, MM] = FunctionK.lift([a] => (fa: M[a]) => fa.mapK(idToEvalK))
      val gk: ~>[MM, M] = FunctionK.lift([a] => (fa: MM[a]) => fa.mapK(evalToIdK))
      given Handle[MM, MiniInt] = summon[Handle[M, MiniInt]].imapK(fk, gk)

      HandleTests[MM, MiniInt](summon).handle[MiniInt]
    }
  )

end HandleSpec
