package classy.optics

// stolen from meow-mtl
class PrismSpec extends classy.BaseSuite:
  enum AppError:
    case HttpMsgError(port: Int, msg: String) extends AppError
    case HttpError(port: Int) extends AppError
    case NoError extends AppError

  test("derived for root") {
    val httpError: AppError.HttpError = AppError.HttpError(1)
    val httpPrism = summon[Prism[AppError, AppError.HttpError]]
    assertEquals(httpPrism.preview(httpError), Some(httpError))
    assertEquals(httpPrism.review(httpError), httpError)

    val httpMsgError: AppError.HttpMsgError = AppError.HttpMsgError(1, "msg")
    val httpMsgPrism = summon[Prism[AppError, AppError.HttpMsgError]]
    assertEquals(httpMsgPrism.preview(httpMsgError), Some(httpMsgError))
    assertEquals(httpMsgPrism.review(httpMsgError), httpMsgError)
  }

  test("derived for first level") {
    val prism = summon[Prism[AppError, Int]] // via HttpError.

    val error = AppError.HttpError(1)

    assertEquals(prism.preview(error), Some(1))
    assertEquals(prism.review(1), error)
  }

  test("derived in ambiguity") {
    enum AmbiguousError:
      case DbAppError(i: Int)
      case Db2AppError(i: Int)

    val prism = summon[Prism[AmbiguousError, Int]] // via DbAppError

    val error = AmbiguousError.DbAppError(1)

    assertEquals(prism.preview(error), Some(1))
    assertEquals(prism.review(1), error)

    // for DB2AppError

    val prism2 = summon[Prism[AmbiguousError, AmbiguousError.Db2AppError]]
    val iso2 = summon[Iso[AmbiguousError.Db2AppError, Int]]

    val error2 = AmbiguousError.Db2AppError(2)
    assertEquals(iso2.composePrism(prism2).preview(error2), Some(2))
    assertEquals(iso2.composePrism(prism2).review(2), error2)
  }

  // test("should fail to compile") {
  //   case class NoConfig()
  //   summon[Prism[NoConfig, Int]]
  // }

end PrismSpec
