package theproject.modules

import cats.effect.*
import doobie.hikari.HikariTransactor
import doobie.util.*
import org.typelevel.log4cats.Logger
import theproject.core.{Chirps, LiveChirps}

final class Core[F[_]] private (val chirps: Chirps[F]) {}

object Core:
  
  private def postgresResource [F[_]: Async: Logger]: Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](32)
    xa <- HikariTransactor
      .newHikariTransactor[F]("org.postgresql.Driver", "jdbc:postgresql:chirpster", "docker", "docker", ec)
  } yield xa
  
  def apply[F[_]: Async: Logger]: Resource[F,Core[F]] =
    postgresResource[F].evalMap(postgres => LiveChirps[F](postgres)).map(chirps => new Core(chirps))

