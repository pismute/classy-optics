package classy.effect

import cats.Eq
import cats.effect.IO

import org.scalacheck.Arbitrary

trait EffectBaseSuite extends munit.CatsEffectSuite with classy.BaseSuite:
  given [A: Arbitrary]: Arbitrary[IO[A]] = Arbitrary(Arbitrary.arbitrary[A].map(x => IO(x)))

  given [A: Eq]: Eq[IO[A]] = Eq.by[IO[A], A](_.unsafeRunSync())

end EffectBaseSuite
