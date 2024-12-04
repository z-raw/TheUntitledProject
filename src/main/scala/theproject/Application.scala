package theproject

import cats.effect.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import theproject.config.Config
import theproject.config.syntax.*

import theproject.modules.{Core, HttpApi}

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run = {
    ConfigSource.default
      .loadF[IO, Config]
      .flatMap { config =>
        val appResource = for {
          core <- Core[IO]
          httpApi <- HttpApi[IO](core)
          server <- EmberServerBuilder
            .default[IO]
            .withHost(config.host)
            .withPort(config.port)
            .withHttpApp(httpApi.routes.orNotFound)
            .build

        }
        yield server

        appResource.useForever
      }
  }
}