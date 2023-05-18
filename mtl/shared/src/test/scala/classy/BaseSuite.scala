package classy

import scala.compiletime.*
import scala.concurrent.Future

import cats.~>
import cats.Comonad
import cats.Eval
import cats.Functor
import cats.Id
import cats.arrow.FunctionK
import cats.syntax.functor.*

import org.scalacheck.Gen

import munit.*

trait BaseSuite extends DisciplineFSuite with AssertionsF with ScalacheckGens:

  override def munitValueTransforms: List[ValueTransform] =
    munitEvalTransform :: super.munitValueTransforms

  // A hack for Matchable marker. munit can not match it with -new-syntax.
  private def matchable(pf: PartialFunction[Matchable, Future[Any]]): PartialFunction[Any, Future[Any]] =
    pf.compose(_.asMatchable)

  private val munitEvalTransform: ValueTransform =
    new ValueTransform(
      "cats.Eval",
      matchable { case e: Eval[?] => Future(e.value)(munitExecutionContext) }
    )

  val evalToIdK: ~>[Eval, Id] = FunctionK.lift[Eval, Id]([a] => (fa: Eval[a]) => fa.value)
  val idToEvalK: ~>[Id, Eval] = FunctionK.lift[Id, Eval]([a] => (fa: Id[a]) => Eval.later(fa))

end BaseSuite

trait AssertionsF:
  self: Assertions =>

  def assertEqualsF[F[_]: Functor, A, B](
      obtained: F[A],
      expected: B,
      clue: => Any = "values are not the same"
  )(using loc: Location, compare: Compare[A, B]): F[Unit] =
    obtained.map(a => assertEquals(a, expected, clue))

  // `extension [F[_]: Functor, A](obtained: F[A])` doesn't work
  implicit class AssertionsFOps[F[_]: Functor, A](obtained: F[A]):
    def assertEquals[B](
        expected: B,
        clue: => Any = "values are not the same"
    )(using loc: Location, compare: Compare[A, B]): F[Unit] =
      assertEqualsF(obtained, expected, clue)

end AssertionsF

trait ScalacheckGens:

  def nonNegNum[A](using num: Numeric[A], c: Gen.Choose[A]): Gen[A] =
    Gen.sized(n => c.choose(num.zero, num.max(num.fromInt(n), num.one)))

end ScalacheckGens
