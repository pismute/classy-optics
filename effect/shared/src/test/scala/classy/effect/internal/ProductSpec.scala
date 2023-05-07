package classy.effect.internal

import cats.Id
import cats.effect.{IO, IOLocal, Ref}
import cats.effect.std.AtomicCell
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.laws.discipline.eq.*
import cats.mtl.{Ask, Stateful}
import cats.mtl.laws.discipline.*

import classy.effect.*
import classy.mtl.*

class ProductSpec extends EffectBaseSuite with classy.ProductData:

  checkAllF(
    "IOLocal.Ask", {
      for
        ioLocal <- IOLocal[Data](Data(MiniInt.zero, true))
        given Ask[IO, Data] = ioLocal.ask
        given Ask[IO, MiniInt] = deriveAsk[IO, Data, MiniInt]
      yield AskTests[IO, MiniInt](summon).ask[MiniInt]
    }
  )

  checkAllF(
    "IOLocal.Stateful", {
      for
        ioLocal <- IOLocal[MiniInt](MiniInt.zero)
        given Stateful[IO, MiniInt] = ioLocal.stateful
      yield StatefulTests[IO, MiniInt](summon).stateful
    }
  )

  checkAllF(
    "Ref.Ask", {
      for
        ref <- Ref.of[IO, Data](Data(MiniInt.zero, true))
        given Ask[IO, Data] = ref.ask
        given Ask[IO, MiniInt] = deriveAsk[IO, Data, MiniInt]
      yield AskTests[IO, MiniInt](summon).ask[MiniInt]
    }
  )

  checkAllF(
    "AtomicCell.Ask", {
      for
        ac <- AtomicCell[IO].of[Data](Data(MiniInt.zero, true))
        given Ask[IO, Data] = ac.ask
        given Ask[IO, MiniInt] = deriveAsk[IO, Data, MiniInt]
      yield AskTests[IO, MiniInt](summon).ask[MiniInt]
    }
  )

  checkAllF(
    "AtomicCell.Stateful", {
      for
        ac <- AtomicCell[IO].of[MiniInt](MiniInt.zero)
        given Stateful[IO, MiniInt] = ac.stateful
      yield StatefulTests[IO, MiniInt](summon).stateful
    }
  )

end ProductSpec
