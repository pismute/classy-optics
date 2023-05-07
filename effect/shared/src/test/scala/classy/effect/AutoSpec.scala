package classy.effect

import cats.effect.{IO, Ref}
import cats.effect.implicits.*

import classy.effect.auto.given

class AutoSpec extends EffectBaseSuite:
  case class Data(i: Int, b: Boolean)

  test("auto ref") {
    for
      given Ref[IO, Data] <- Ref.of[IO, Data](Data(1, true))
      _ = summon[Ref[IO, Int]]
    yield ()
  }

end AutoSpec
