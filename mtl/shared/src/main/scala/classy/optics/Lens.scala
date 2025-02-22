package classy.optics

import scala.annotation.implicitNotFound

/** generate a Lens optic for a product
  *
  * {{{
  *   case class ProductType(i: Int, b: String)
  *
  *   summon[Lens[ProductType, Int]]
  *   summon[Lens[ProductType, String]]
  * }}}
  *
  * It won't be compiled if your product type is ambiguous:
  *
  * {{{
  *   case class ProductType(i1: Int, i2: Int)
  *
  *   summon[Lens[ProductType, Int]] // error
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Lens[${S}, ${A}].\n It is intended to be derived automatically,\n you should check your product type if `case class ${S}(a: ${A}, ...)`"
)
trait Lens[S, A] extends Getter[S, A]:
  def set: S => A => S

  inline def modify[B](s: S)(f: A => (A, B)): (S, B) =
    val (a, b) = f(view(s))
    (set(s)(a), b)

  inline def update(s: S)(f: A => A): S = set(s)(f(view(s)))

private[classy] object Lens:
  class UnnamedLens[S, A](_view: S => A)(_set: S => A => S) extends Lens[S, A]:
    override def view: S => A = _view
    override def set: S => A => S = _set

  inline def apply[S, A](_view: S => A)(_set: S => A => S): Lens[S, A] = new UnnamedLens[S, A](_view)(_set)

  inline given derived[T <: Product, A]: Lens[T, A] = ${ ProductMacros.genLens[T, A] }
end Lens
