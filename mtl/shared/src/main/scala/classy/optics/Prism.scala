package classy.optics

import scala.annotation.implicitNotFound

/** generate a Prism optic for a product
  *
  * {{{
  *   enum AppError:
  *     case HttpAppError(httpError: HttpError)
  *     case DbAppError(dbError: DbError)
  *
  *   summon[Prism[AppError, HttpAppError]]
  *   summon[Prism[AppError, HttpError]]
  *   summon[Prism[AppError, DbAppError]]
  *   summon[Prism[AppError, DbError]]
  * }}}
  *
  * It won't be compiled if your product type is ambiguous:
  *
  * {{{
  *   enum AppError:
  *     case HttpAppError(httpError: HttpError, msg: String)
  *
  *   summon[Prism[ProductType, HttpError] // error, because HttpAppError is not isomorphic.
  *   summon[Prism[ProductType, String] // error, because HttpAppError is not isomorphic.
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
  *   summon[Prism[ProductType, DbError] // it will pick it via DbAppError always
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Prism[${S}, ${A}].\n It is intended to be derived automatically,\n you should check your sum type if `enum ${S}: case ${A}` or `enum ${S}: case Something(a: ${A})`"
)
trait Prism[S, A] extends Review[S, A]:
  def preview: S => Option[A]

  def unapply(s: S): Option[A] = preview(s)

private[classy] object Prism:
  class UnnamedPrism[S, A](_preview: (S) => Option[A])(_review: A => S) extends Prism[S, A]:
    override def preview: S => Option[A] = _preview
    override def review: A => S = _review

  inline def apply[S, A](_preview: (S) => Option[A])(_review: A => S): Prism[S, A] =
    new UnnamedPrism[S, A](_preview)(_review)

  inline def fromPF[S, A](_preview: => PartialFunction[S, A])(_review: A => S): Prism[S, A] =
    apply[S, A](_preview.lift)(_review)

  inline given derived[S, A]: Prism[S, A] = ${ SumMacros.genPrism[S, A] }
end Prism
