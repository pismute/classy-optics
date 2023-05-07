package classy

import cats.laws.discipline.*

trait SumData:
  type Data = Either[MiniInt, String]

end SumData
