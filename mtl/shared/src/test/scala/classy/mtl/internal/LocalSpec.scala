package classy.mtl.internal

import cats.Eval
import cats.arrow.FunctionK
import cats.data.Reader
import cats.data.ReaderT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.laws.discipline.eq.*
import cats.mtl.Local
import cats.mtl.laws.discipline.*
import cats.~>
import classy.mtl.*
import org.scalacheck.Arbitrary

class LocalSpec extends ProductBaseSuite with classy.ProductData:
  type M[A] = Reader[Data, A]
  given Local[M, MiniInt] = deriveLocal[M, Data, MiniInt]

  checkAll(
    "Local",
    LocalTests[M, MiniInt](summon).local[MiniInt, MiniInt]
  )

  checkAll(
    "Local.imapK", {
      type MM[A] = ReaderT[Eval, Data, A]
      val fk: ~>[M, MM] = FunctionK.lift([a] => (fa: M[a]) => fa.mapK(idToEvalK))
      val gk: ~>[MM, M] = FunctionK.lift([a] => (fa: MM[a]) => fa.mapK(evalToIdK))
      given Local[MM, MiniInt] = summon[Local[M, MiniInt]].imapK(fk, gk)

      LocalTests[MM, MiniInt](summon).local[MiniInt, MiniInt]
    }
  )

end LocalSpec
