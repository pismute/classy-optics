package classy.optics

// stolen from meow-mtl
class GetterSpec extends classy.BaseSuite:
  case class HttpConfig(port: Int, address: String)

  type TupleConfig = Tuple1[Int]

  test("derived for first level") {
    val config = HttpConfig(1, "address")

    assertEquals(summon[Getter[HttpConfig, String]].view(config), "address")

    val tConfig = Tuple1(1)
    assertEquals(summon[Getter[TupleConfig, Int]].view(tConfig), 1)
  }

  // test("should fail to compile") {
  //   enum AppError:
  //     case HttpError
  //   case class Ambiguous(i1: Int, I2: Int)
  //   case class NoConfig()

  //   summon[Getter[HttpConfig, Double]]
  //   summon[Getter[NoConfig, Int]]
  //   summon[Getter[AppError, Int]]
  //   summon[Getter[Ambiguous, Int]]
  // }

end GetterSpec
