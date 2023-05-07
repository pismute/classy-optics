package classy.effect.internal

import cats.effect.{IO, IOLocal, Ref}
import cats.effect.std.AtomicCell
import cats.laws.discipline.*
import cats.laws.discipline.arbitrary.*
import cats.laws.discipline.eq.*
import cats.mtl.Tell
import cats.mtl.laws.discipline.*

import classy.effect.*
import classy.mtl.*

class SumSpec extends EffectBaseSuite with classy.SumData:

  checkAllF(
    "IOLocal.Tell", {
      for
        ioLocal <- IOLocal[Data](Left(MiniInt.zero))
        given Tell[IO, Data] = ioLocal.tell
        given Tell[IO, MiniInt] = deriveTell[IO, Data, MiniInt]
      yield TellTests[IO, MiniInt](summon).tell[MiniInt]
    }
  )

  checkAllF(
    "Ref.Tell", {
      for
        ref <- Ref.of[IO, Data](Left(MiniInt.zero))
        given Tell[IO, Data] = ref.tell
        given Tell[IO, MiniInt] = deriveTell[IO, Data, MiniInt]
      yield TellTests[IO, MiniInt](summon).tell[MiniInt]
    }
  )

  checkAllF(
    "AtomicCell.Tell", {
      for
        ac <- AtomicCell[IO].of[Data](Left(MiniInt.zero))
        given Tell[IO, Data] = ac.tell
        given Tell[IO, MiniInt] = deriveTell[IO, Data, MiniInt]
      yield TellTests[IO, MiniInt](summon).tell[MiniInt]
    }
  )

end SumSpec
