package classy.mtl.internal

import cats.{~>, Id}
import cats.arrow.FunctionK
import cats.data.{State, StateT}
import cats.derived.derived
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Stateful
import cats.mtl.laws.discipline.*

import org.scalacheck.Arbitrary

import classy.BaseSuite
import classy.mtl.*

class StatefulSpec extends ProductBaseSuite with classy.ProductData:
  type M[A] = State[Data, A]
  given [F[_]](using Stateful[F, Data]): Stateful[F, MiniInt] = deriveStateful

  checkAll(
    "Stateful",
    StatefulTests[M, MiniInt](summon).stateful
  )

  val statefulOfMAndMiniInt = summon[Stateful[M, MiniInt]]
  checkAll(
    "Stateful.mapK", {
      type F[A] = StateT[Id, Data, A]
      val fk: ~>[M, F] = FunctionK.lift([a] => (ma: M[a]) => StateT.fromState[Id, Data, a](ma))
      given Stateful[F, MiniInt] = statefulOfMAndMiniInt.mapK(fk)

      StatefulTests[F, MiniInt](summon).stateful
    }
  )

end StatefulSpec
