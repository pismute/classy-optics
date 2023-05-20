package classy.mtl.internal

import cats.{Eval, Id}
import cats.~>
import cats.arrow.FunctionK
import cats.data.StateT
import cats.derived.derived
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Stateful
import cats.mtl.laws.discipline.*

import org.scalacheck.Arbitrary

import classy.BaseSuite
import classy.mtl.*

class StatefulSpec extends ProductBaseSuite with classy.ProductData:
  type M[A] = StateT[Id, Data, A]
  given [F[_]](using Stateful[F, Data]): Stateful[F, MiniInt] = deriveStateful

  checkAll(
    "Stateful",
    StatefulTests[M, MiniInt](summon).stateful
  )

  val statefulOfMAndMiniInt = summon[Stateful[M, MiniInt]]
  checkAll(
    "Stateful.mapK", {
      type MM[A] = StateT[Eval, Data, A]
      val fk: ~>[M, MM] = FunctionK.lift([a] => (fa: M[a]) => fa.mapK(idToEvalK))
      given Stateful[MM, MiniInt] = statefulOfMAndMiniInt.mapK(fk)

      StatefulTests[MM, MiniInt](summon).stateful
    }
  )

end StatefulSpec
