package theproject.modules

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.typelevel.log4cats.Logger
import theproject.config.PostgresConfig

object Database:

  def makePostgresResource [F[_]: Async: Logger](config: PostgresConfig): Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](config.nThreads)
    xa <- HikariTransactor
      .newHikariTransactor[F](config.driver, config.url, config.user, config.password, ec)
  } yield xa