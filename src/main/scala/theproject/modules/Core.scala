package theproject.modules

import cats.effect.*
import cats.implicits.*
import cats.syntax.all.*

import doobie.hikari.HikariTransactor
import doobie.util.*
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger
import theproject.core.{Chirps, LiveChirps}

final class Core[F[_]] private (val chirps: Chirps[F]) {}

object Core:
  
  def apply[F[_]: Async: Logger](xa: Transactor[F]): Resource[F,Core[F]] =
    Resource.eval(LiveChirps[F](xa)).map(chirps => new Core(chirps))

