# classy-optics
![Maven](https://img.shields.io/maven-central/v/io.github.pismute/classy-mtl_3.svg?style=flat-square)

It is built for [meow-mtl] again in Scala 3.

It provides the similar functionality with [meow-mtl], but limited magics in Scala 3:

Available for Scala JVM, Scalajs and Scala Native:

```scala
libraryDependencies += "io.github.pismute" %% "classy-mtl" % "<version>"
libraryDependencies += "io.github.pismute" %% "classy-effect" % "<version>"
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
   given Ask[Appt, AppEnv] = ref.ask
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

## Intersection Type.

An intersection type is a type alias, it is a product-like alias. We can think the cardinality of `ProductLike` is 4 as of a product of `HasBoolean1`'s one and `HasBoolean2`'s one:

```scala
case class HasBoolean1(b: Boolean)
case class HasBoolean2(b: Boolean)

type ProductLike = HasBoolean1 & HasBoolean2
```

Maybe, We might use Intersection type in our data. Compiler does not provide goodies like `case class`, though, there is some free lunches from the type inference. For example:

```scala
trait DbConfig:
  def dbPort: Int
  
trait HttpConfig:
  def httpPort: Int
  
case case AppConfigCake(dbPort: Int, httpPort: Int) extends DbConfig with HttpConfig derives Show

type AppConfig = DbConfig & HttpConfig
```

`AppConfigCake` is needed to materialize AppConfig. Also we can take `case class`'s goodies like `derives Show`, in this case.

As a product type, intersection types can have optics. AppConfig is visible as DbConfig or HttpConfig. So, intersection types can have `Getter` instances. If it has `Getter` instances, then we can derive Ask instances automatically. It is the idea of this library.

However, we do not need even `Getter` instances for intersection types to have Ask instances, because Ask is covariant. `Ask[F[_], +E]`'s `E` is covariant. When we ask Scala compiler to bring Ask instances from `given` space, the compiler will bring the instance what exactly we want. The compiler will bring `Ask[F[_], AppConfig]` instance for `Ask[F[_], DbConfig]` without optics. It is free lunches, free optics.

Intersection types can not have `Lens` optic, because they do not have the standard way of mutation, which is a way of `set`. Without `Lens` optics, narrowing `Local` and `Stateful` can not work with intersection types.

So, `classy-optics` does not provide any optics for intersection types.

## Union Type.

Union type is a sum-like alias, the cardinality of `AppError` is 4 as of a sum of `DbError` and `HttpError`:

```scala
enum DbError:
  case LostConnection, ExceedLimit
  
enum HttpError:
  case LostConnection, ExceedLimit

type AppError = DbError | HttpError
```

Unlike intersection types, Mutation is not required for Prism types. They can have both of a `Prism` instance and a `Review` instance. It is simple:

```scala
class APrism[AppError, DbError] extends Prism[AppError, DbError]:
  def preview: AppError => Option[DbError] = { case x: DbError => x }.lift
  def review: DbError => AppError = x => x
```

Also, like `Ask[F[_], +E]` type class, the parameters of `Raise[F[_], -E]` and `Tell[F[_], -L]` are contravariant. So deriving their sub instances is not needed. If `Raise[F[_], AppError]` and `Tell[F[_], AppError]` exists in given space. Compiler will return those instances for `Raise[F[_], DbError]`,  `Tell[F[_], DbError]` and so on. In case of `Raise` and `Tell`, we do not need optics.

But, `Handle[F[_], E]`'s type parameter is invariant. we will not get `Handle[F[_], AppError]` instance for `Handle[F[_], DbError]`. So, `Handle` need a Prism optic to narrow it. 

Now, `classy-optics` provides Prism instances for Union types. It works seamlessly:

```scala
type AppError = DbError | HttpError 

summon[Raise[F, DbError]] // works if Raise[F, AppError] exists

// works because Prism[AppError, DbError] is derived automatically
given [F[_]](using Handle[F, AppError]): Raise[F, DbError] = deriveHandle
```

Ok, it looks simpler than `enum AppError`. But, yet, it is not. There are no instances for essential type classes like `Eq`, `Show` and so on. The instances need to be provided manually.

## License
MIT

[cats-effect]: https://github.com/typelevel/cats-effect
[cats-mtl]: https://github.com/typelevel/cats-mtl
[meow-mtl]: https://github.com/oleg-py/meow-mtl
[mtl-talk]: https://www.youtube.com/watch?v=GZPup5Iuaqw
