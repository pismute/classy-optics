package classy.mtl.internal

import cats.~>
import cats.arrow.FunctionK
import cats.data.Reader
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.laws.discipline.eq.*
import cats.mtl.Local
import cats.mtl.laws.discipline.*

import org.scalacheck.Arbitrary

import classy.mtl.*

class LocalSpec extends ProductBaseSuite with classy.ProductData:
  type M[A] = Reader[Data, A]
  given Local[M, MiniInt] = deriveLocal[M, Data, MiniInt]

  checkAll(
    "Local",
    LocalTests[M, MiniInt](summon).local[MiniInt, MiniInt]
  )

  checkAll(
    "Local.imapK", {
      type F[A] = Data => A
      val fk: ~>[M, F] = FunctionK.lift([a] => (ma: M[a]) => ma.run)
      val gk: ~>[F, M] = FunctionK.lift([a] => (fa: F[a]) => Reader(fa))
      given Local[F, MiniInt] = summon[Local[M, MiniInt]].imapK(fk, gk)

      LocalTests[F, MiniInt](summon).local[MiniInt, MiniInt]
    }
  )

end LocalSpec
