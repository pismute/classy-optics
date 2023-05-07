package classy.optics

// stolen from meow-mtl
class LensSpec extends classy.BaseSuite:
  case class HttpConfig(port: Int)
  case class AppConfig(name: String, httpConfig: HttpConfig)
  case class NoConfig()

  type TupleConfig = (Int, String)

  test("derived for first level") {
    val httpConfig = HttpConfig(1)
    val config = AppConfig("name", httpConfig)

    assertEquals(summon[Lens[AppConfig, String]].view(config), "name")
    assertEquals(summon[Lens[AppConfig, HttpConfig]].view(config), httpConfig)

    val tConfig = (1, "string")
    assertEquals(summon[Lens[TupleConfig, Int]].view(tConfig), 1)
    assertEquals(summon[Lens[TupleConfig, Int]].set(tConfig)(2), (2, "string"))
  }

  // test("should fail to compile") {
  //   enum AppError:
  //     case HttpError
  //   case class Ambiguous(i1: Int, I2: Int)

  //   summon[Lens[AppConfig, Int]]
  //   summon[Lens[NoConfig, Int]]
  //   summon[Lens[AppError, Int]]
  //   summon[Lens[Ambiguous, Int]]
  // }

end LensSpec
