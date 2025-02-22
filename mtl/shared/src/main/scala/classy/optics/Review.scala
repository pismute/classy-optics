package classy.optics

import scala.annotation.implicitNotFound

/** generate a Prism optic for a product
  *
  * {{{
  *   enum AppError:
  *     case HttpAppError(httpError: HttpError)
  *     case DbAppError(dbError: DbError)
  *
  *   summon[Review[AppError, HttpAppError]]
  *   summon[Review[AppError, HttpError]]
  *   summon[Review[AppError, DbAppError]]
  *   summon[Review[AppError, DbError]]
  * }}}
  *
  * It won't be compiled if your product type is ambiguous:
  *
  * {{{
  *   enum AppError:
  *     case HttpAppError(httpError: HttpError, msg: String)
  *
  *   summon[Review[AppError, HttpError]] // error, because HttpAppError is not isomorphic.
  *   summon[Review[AppError, String]] // error, because HttpAppError is not isomorphic.
  * }}}
  *
  * If your sum type has ambiguity, it doesn't check the ambiguity. The first element will be picked in the defined
  * order.
  *
  * {{{
  *   enum AppError:
  *     case DbAppError(dbError: DbError)
  *     case Db2AppError(dbError: DbError)
  *
  *   summon[Review[AppError, DbError]] // it will pick it via DbAppError always
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Review[${S}, ${A}].\n It is intended to be derived automatically,\n you should check your sum type if `enum ${S}: case ${A}` or `enum ${S}: case Something(a: ${A})`"
) trait Review[S, A]:
  def review: A => S

private[classy] object Review:
  class UnnamedReview[S, A](_review: A => S) extends Review[S, A]:
    override def review: A => S = _review

  inline def apply[S, A](_review: A => S): Review[S, A] = new UnnamedReview[S, A](_review)

  inline given derived[S, A]: Review[S, A] = ${ SumMacros.genReview[S, A] }
end Review
