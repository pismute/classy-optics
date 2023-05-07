package classy.optics

// stolen from meow-mtl
class ReviewSpec extends classy.BaseSuite:
  enum AppError:
    case HttpMsgError(port: Int, msg: String) extends AppError
    case HttpError(port: Int) extends AppError
    case NoError extends AppError

  test("derived for root") {
    val httpError: AppError.HttpError = AppError.HttpError(1)
    val httpPrism = summon[Review[AppError, AppError.HttpError]]
    assertEquals(httpPrism.review(httpError), httpError)

    val httpMsgError: AppError.HttpMsgError = AppError.HttpMsgError(1, "msg")
    val httpMsgPrism = summon[Review[AppError, AppError.HttpMsgError]]
    assertEquals(httpMsgPrism.review(httpMsgError), httpMsgError)
  }

  test("derived for first level") {
    val review = summon[Review[AppError, Int]] // via HttpError.

    val error = AppError.HttpError(1)

    assertEquals(review.review(1), error)
  }

  test("derived in ambiguity") {
    enum AmbiguousError:
      case DbAppError(i: Int)
      case Db2AppError(i: Int)

    val review = summon[Review[AmbiguousError, Int]] // via DbAppError

    val error = AmbiguousError.DbAppError(1)

    assertEquals(review.review(1), error)

    // for DB2AppError

    val review2 = summon[Review[AmbiguousError, AmbiguousError.Db2AppError]]
    val iso2 = summon[Iso[AmbiguousError.Db2AppError, Int]]

    val error2 = AmbiguousError.Db2AppError(2)
    assertEquals(iso2.composeReview(review2).review(2), error2)
  }

  // test("should fail to compile") {
  //   case class NoConfig()
  //   summon[Review[NoConfig, Int]]
  // }

end ReviewSpec
