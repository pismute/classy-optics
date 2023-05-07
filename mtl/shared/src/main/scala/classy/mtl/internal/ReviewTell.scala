package classy.mtl.internal

import cats.Functor
import cats.mtl.Tell

import classy.optics.Review

private[classy] class ReviewTell[F[_], A, B](parent: Tell[F, A], review: Review[A, B]) extends Tell[F, B]:

  inline def functor: Functor[F] = parent.functor

  def tell(b: B): F[Unit] = parent.tell(review.review(b))

end ReviewTell
