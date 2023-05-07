package classy.optics

import scala.annotation.implicitNotFound

/** generate a Getter optic for a product
  *
  * {{{
  *   case class ProductType(i: Int, b: String)
  *
  *   summon[Getter[ProductType, Int]]
  *   summon[Getter[ProductType, String]]
  * }}}
  *
  * It won't be compiled if your product type is ambiguous:
  *
  * {{{
  *   case class ProductType(i1: Int, i2: Int)
  *
  *   summon[Getter[ProductType, Int]] // error
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Getter[${S}, ${A}].\n It is intended to be derived automatically,\n you should check your product type if `case class ${S}(a: ${A}, ...)`"
)
trait Getter[S, A]:
  def view: S => A

private[classy] object Getter:
  inline def apply[S, A](_view: S => A): Getter[S, A] = new Getter[S, A]:
    def view: S => A = _view

  inline given derived[T <: Product, A]: Getter[T, A] = ${ ProductMacros.genGetter[T, A] }
