package classy.optics

import scala.annotation.implicitNotFound

import Tuple.*

/** generate an Iso optic for a isomorphic product
  *
  * {{{
  *   case class ProductType(single: Any)
  *
  *   summon[Iso[ProductType, Any]]
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Iso[${S}, ${A}].\n It is intended to be derived automatically,\n you should check your product type if `case class ${S}(a: ${A})`"
)
trait Iso[S, A] extends Getter[S, A] with Review[S, A]:
  outer =>

  inline def compose[T](other: Iso[T, S]): Iso[T, A] =
    Iso[T, A](outer.view.compose(other.view))(other.review.compose(outer.review))

  inline def composePrism[B](other: Prism[B, S]): Prism[B, A] =
    Prism[B, A](b => other.unapply(b).map(outer.view))(other.review.compose(review))

  inline def composeReview[B](other: Review[B, S]): Review[B, A] =
    Review[B, A](other.review.compose(outer.review))

private[classy] object Iso:
  inline def apply[S, A](_view: S => A)(_review: A => S): Iso[S, A] = new Iso[S, A]:
    def view: S => A = _view
    def review: A => S = _review

  inline given derived[T <: Product, A]: Iso[T, A] = ${ ProductMacros.genIso[T, A] }
