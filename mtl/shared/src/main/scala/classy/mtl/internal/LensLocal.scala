package classy.mtl.internal

import cats.mtl.Local

import classy.optics.Lens

private[classy] class LensLocal[F[_], A, B](parent: Local[F, A], lens: Lens[A, B])
    extends GetterAsk(parent, lens)
    with Local[F, B]:

  def local[C](fa: F[C])(f: B => B): F[C] = parent.local(fa)(lens.update(_)(f))

end LensLocal
