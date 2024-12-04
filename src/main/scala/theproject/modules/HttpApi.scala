package theproject.modules

import cats.effect.*
import cats.syntax.all.*
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import theproject.http.routes.{ChirpRoutes, HealthRoutes}

class HttpApi[F[_]: Async: Logger] private(core: Core[F]):
  
  private val healthRoutes = HealthRoutes[F].routes
  private val chirpRoutes = ChirpRoutes[F](core.chirps).routes

  val routes = Router("/api" -> (healthRoutes <+> chirpRoutes))

object HttpApi:
  def apply[F[_]: Async: Logger](core: Core[F]): Resource[F, HttpApi[F]] = Resource.pure(new HttpApi[F](core))