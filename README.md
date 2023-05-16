# classy-optics
![Maven](https://img.shields.io/maven-central/v/pismute/classy-mtl_3.svg?style=flat-square)

It is built for [meow-mtl] again in Scala 3.

It provides the similar functionality with [meow-mtl], but limited magics in Scala 3:

Available for Scala JVM, Scalajs and Scala Native:

```scala
libraryDependencies += "io.github.pismute" %% "classy-mtl" % "0.1.0"
libraryDependencies += "io.github.pismute" %% "classy-effect" % "0.1.0"
```

Inspired by [Next-level MTL talk][mtl-talk] and [meow-mtl]

### Quick Example

```scala
import classy.mtl.*

object app:
  case class AppEnv(httpEnv: HttpEnv, dbEnv: DbEnv)
  given [F[_]](using Ask[F, AppEnv]): Ask[F, HttpEnv] = deriveAsk
  given [F[_]](using Ask[F, AppEnv]): Ask[F, DbEnv] = deriveAsk

  enum AppError derives Show:
    case AppHttpError(error: HttpError)
    case AppDbError(error: DbError)
  given [F[_]](using Raise[F, AppError]): Raise[F, HttpError] = deriveRaise
  given [F[_]](using Raise[F, AppError]): Raise[F, DbError] = deriveRaise

import app.*

object appt:
  private type App[A] = EitherT[ReaderT[IO, AppEnv, *], AppError, A]
  opaque type AppT[A] = App[A]

  object AppT:
    def apply[A](x: App[A]): AppT[A] = x

    given (using inst: Ask[App, AppEnv]): Ask[AppT, AppEnv] = inst
    given (using inst: Async[App]): Async[AppT] = inst
    given (using inst: LiftIO[App]): LiftIO[AppT] = inst
    given (using inst: Raise[App, AppError]): Raise[AppT, AppError] = inst

import appt.*

class HttpService[F[_]](using ask: Ask[F, HttpEnv], raise: Raise[F, HttpError]):
  ...
end HttpService

class DbService[F[_]](using ask: Ask[F, DbEnv], raise: Raise[F, DbError]):
  ...
end DbService

val httpService = HttpService[AppT]
val DbService = DbService[AppT]
```

## Classy optics

`classy-optics` has the same goal with [meow-mtl], but focused on having less ambiguity in Scala 3. `classy-optics` can narrow MTL type classes with classy optics, Optic instances are automatically generated. It does not support identical optics because we do not need regenerate existing MTL instances. Also, It gives compilation error on ambiguous data type as possible.

### Product Examples

```scala
case class Mono(i: Int)

summon[Getter[Mono, Int]]
summon[Lens[Mono, Int]]
summon[Iso[Mono, Int]]

case class MyProduct(i: Int, s: String)

summon[Getter[MyProduct, Int]]
summon[Getter[MyProduct, String]]
summon[Lens[MyProduct, Int]]
summon[Lens[MyProduct, String]]

summon[Iso[MyProduct, Int]] // error because MyProduct is not monomorphic.

summon[Getter[MyProduct, MyProduct]] // error
summon[Lens[MyProduct, MyProduct]] // error
summon[Iso[MyProduct, MyProduct]] // error

case class AmbiguousProduct(i1: Int, i2: Int)

summon[Getter[AmbiguousProduct, Int]] // error
summon[Lens[AmbiguousProduct, Int]] // error
```

### Sum Examples

```scala
enum HttpError:
  case TooManyRequest(n: Int)
  case Unauthorized

enum DbError:
  case LostConnection, QueryTimeout

enum AppError:
  case DbAppError(error: DbError)
  case HttpAppError(error: HttpError)
  case AnotherAppError(code: Int, msg: String)

summon[Review[AppError, DbError]]
summon[Review[AppError, HttpError]]
summon[Prism[AppError, DbError]]
summon[Prism[AppError, HttpError]]

summon[Review[AppError, AppError.AnotherAppError]]
summon[Prism[AppError, AppError.AnotherAppError]]

summon[Review[AppError, Int]] // error
summon[Review[AppError, String]] // error
summon[Prism[AppError, Int]] // error
summon[Prism[AppError, String]] // error
```

`classy-optics` does not traverse all hierarchical tree. It looks for only first level, and root level for sum types. So data hierarchy should be kept along with application hierarchy:

```scala
summon[Prism[AppError, HttpError.TooManyRequest]] // error on second level.
```

### Derivation of MTL type classes

`classy-optics` derive MTL instances via optics from base instances

```scala
import classy.mtl.*

case class MyType(subtype: MySubtype)
given Ask[AppT, MyType] = ...
given Ask[AppT, MySubType] = deriveAsk
summon[Ask[AppT, MySubType]]
```

Automatic derivation is also supported:

```scala
import classy.mtl.auto.*

case class MyType(subtype: MySubtype)
given Ask[AppT, MyType] = ...
summon[Ask[AppT, MySubType]]
```

Supported typeclasses:

| Typeclass | Required optic |
|-----------|----------------|
| Handle    | Prism          |
| Raise     | Review         |
| Tell      | Review         |
| Ask       | Getter         |
| Local     | Lens           |
| Stateful  | Lens           |

## Cats-effect

`classy-effect` provides MTL instances for some cats effect types:

| cats-effect   | Mtl                 |
|---------------|---------------------|
| AtomicCell    | Ask, Stateful, Tell |
| IOLocal       | Ask, Stateful, Tell |
| Ref           | Ask, Ref(?), Tell   |


```scala
case class AppEnv(httpEnv: HttpEnv, dbEnv: DbEnv)
given [F[_]](using Ask[F, AppEnv]): Ask[F, HttpEnv] = deriveAsk

for
   ref <- Ref.of[AppT, AppEnv](AppEnv(...))
   given Ask[Appt, Data] = ref.ask
yield ... 

```

`Ref` could not have `Stateful` instance because `Stateful` is not atomic, see https://github.com/typelevel/cats-mtl/pull/120.
Instead, we can use `Ref` directly like the other MTL type classes.

```scala
import classy.effect.*

case class AppCache(http: HttpCache, db: DbCache)
given [F[_]](using Ref[F, AppCache]): Ref[F, HttpCache] = deriveRef

for
  given Ref[AppT, AppCache] <- Ref.of[AppT, AppCache](...)
  _ = summon[Ref[AppT, HttpCache]]
yield ... 
```

## License
MIT

[cats-effect]: https://github.com/typelevel/cats-effect
[cats-mtl]: https://github.com/typelevel/cats-mtl
[meow-mtl]: https://github.com/oleg-py/meow-mtl
[mtl-talk]: https://www.youtube.com/watch?v=GZPup5Iuaqw
