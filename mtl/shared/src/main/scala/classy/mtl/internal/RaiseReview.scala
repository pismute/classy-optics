package classy.mtl.internal

import cats.Functor
import cats.mtl.Raise

import classy.optics.Review

private[classy] open class ReviewRaise[F[_], A, B](parent: Raise[F, A], review: Review[A, B]) extends Raise[F, B]:

  def functor: Functor[F] = parent.functor

  def raise[B1 <: B, C](b: B1): F[C] = parent.raise(review.review(b))

end ReviewRaise
