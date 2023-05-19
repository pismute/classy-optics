package classy.optics

import scala.quoted.*
import scala.quoted.runtime.*

private[classy] object Macros:
  def showTypeAlias[T: Type](using Quotes): String =
    import quotes.reflect.*

    s"type ${Type.show[T]} = ${TypeRepr.of[T].dealias.show}"

  def assertNonIdenticalType[A: Type, B: Type](using Quotes): Unit =
    import quotes.reflect.*

    if TypeRepr.of[A].=:=(TypeRepr.of[B]) then
      report.errorAndAbort(s"${Type.show[A]} must not be identical to ${Type.show[B]}")
    else ()

end Macros
