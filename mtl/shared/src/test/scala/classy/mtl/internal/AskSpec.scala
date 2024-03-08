package classy.mtl.internal

import cats.~>
import cats.Eval
import cats.arrow.FunctionK
import cats.data.Reader
import cats.data.ReaderT
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.mtl.Ask
import cats.mtl.laws.discipline.*

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
      type MM[A] = ReaderT[Eval, Data, A]
      val fk: ~>[M, MM] = FunctionK.lift([a] => (fa: M[a]) => fa.mapK(idToEvalK))
      given Ask[MM, MiniInt] = summon[Ask[M, MiniInt]].mapK(fk)

      AskTests[MM, MiniInt](summon).ask[MiniInt]
    }
  )

  test("covariant test on a cake") {
    trait HasInt:
      def int: Int

    trait HasString:
      def string: String

    class Config(val int: Int, val string: String) extends HasInt with HasString

    type MM[A] = Reader[Config, A]

    summon[Ask[MM, HasInt]]

    summon[Ask[MM, HasString]]
  }

  test("covariant test on a intersection type") {
    trait HasInt:
      def int: Int

    trait HasString:
      def string: String

    type Config = HasInt & HasString

    type MM[A] = Reader[Config, A]

    summon[Ask[MM, HasInt]]

    summon[Ask[MM, HasString]]
  }

end AskSpec
