package theproject

import cats.effect.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import theproject.config.{AppConfig, EmberConfig}
import theproject.config.syntax.*
import theproject.modules.{Core, Database, HttpApi}

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run = {
    ConfigSource.default
      .loadF[IO, AppConfig]
      .flatMap {
        case AppConfig(emberConfig, postgresConfig) =>
          val appResource = for {
            xa <- Database.makePostgresResource[IO](postgresConfig)
            core <- Core[IO](xa)
            httpApi <- HttpApi[IO](core)
            server <- EmberServerBuilder
              .default[IO]
              .withHost(emberConfig.host)
              .withPort(emberConfig.port)
              .withHttpApp(httpApi.routes.orNotFound)
              .build

          }
          yield server

          appResource.use(_ => IO.println("Backend is up...!") *> IO.never)
      }
  }
}