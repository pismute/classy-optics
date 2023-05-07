package classy.mtl.internal

import cats.{~>, Eq, Id}
import cats.arrow.FunctionK
import cats.data.Reader
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Ask
import cats.mtl.laws.discipline.*

import org.scalacheck.Arbitrary

import classy.mtl.*

class AskSpec extends ProductBaseSuite with classy.ProductData:
  type M[A] = Reader[Data, A]
  given Ask[M, MiniInt] = deriveAsk[M, Data, MiniInt]

  checkAll(
    "Ask",
    AskTests[M, MiniInt](summon).ask[MiniInt]
  )

  checkAll(
    "Ask.mapK", {
      type F[A] = Data => A
      val fk: ~>[M, F] = FunctionK.lift([a] => (ma: M[a]) => ma.run)
      given Ask[F, MiniInt] = summon[Ask[M, MiniInt]].mapK(fk)

      AskTests[F, MiniInt](summon).ask[MiniInt]
    }
  )

end AskSpec
