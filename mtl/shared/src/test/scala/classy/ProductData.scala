package classy

import cats.Eq
import cats.derived.derived
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.laws.discipline.eq.*

import org.scalacheck.*

trait ProductData:
  case class Data(i: MiniInt, b: Boolean) derives Eq

  given Cogen[Data] = Cogen { (seed: rng.Seed, data: Data) =>
    val seed1 = Cogen.perturb(seed, data.i)
    Cogen.perturb(seed1, data.b)
  }

  given Arbitrary[Data] =
    Arbitrary {
      for
        i <- Arbitrary.arbitrary[MiniInt]
        b <- Arbitrary.arbitrary[Boolean]
      yield Data(i, b)
    }

end ProductData
