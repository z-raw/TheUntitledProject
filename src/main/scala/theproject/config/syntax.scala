package theproject.config

import cats.syntax.all.*
import cats.MonadThrow
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.ConfigReaderException

import scala.reflect.ClassTag

object syntax:
  extension (source: ConfigSource)
    def loadF[F[_], A: ClassTag: ConfigReader](using F: MonadThrow[F]): F[A] =
      F.pure(source.load[A])
        .flatMap:
          case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
          case Right(value) => F.pure(value)
