package classy.optics

// stolen from meow-mtl
class IsoSpec extends classy.BaseSuite:
  case class DbConfig(address: String, port: Int)
  case class HttpConfig(port: Int)
  case class NoConfig()

  type TupleConfig = Tuple1[Int]

  test("derived for first level") {
    val config = HttpConfig(1)

    assertEquals(summon[Iso[HttpConfig, Int]].view(config), 1)
    assertEquals(summon[Iso[HttpConfig, Int]].review(1), config)

    val tConfig = Tuple1(1)
    assertEquals(summon[Iso[TupleConfig, Int]].view(tConfig), 1)
    assertEquals(summon[Iso[TupleConfig, Int]].review(1), tConfig)
  }

  // test("should fail to compile") {
  //   summon[Iso[DbConfig, Int]]
  //   summon[Iso[NoConfig, Int]]
  // }

end IsoSpec
