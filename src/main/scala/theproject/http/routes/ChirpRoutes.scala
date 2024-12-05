package theproject.http.routes

import cats.effect.*
import cats.syntax.all.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.server.*
import org.typelevel.log4cats.Logger

import theproject.core.Chirps
import theproject.domain.chirps.{Chirp, ChirpInfo}
import theproject.http.responses.FailureResponse
import theproject.http.validation.syntax.*

import java.util.UUID

class ChirpRoutes[F[_] : Concurrent : Logger] private(chirps: Chirps[F]) extends HttpValidationDsl[F]:

  private val allChirpsRoute: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root =>
      chirps.all.flatMap(Ok(_))


  private val findChirpRoute: HttpRoutes[F] = HttpRoutes.of[F]:
    case req@GET -> Root / UUIDVar(id) =>

      for {
        chirp <- chirps.find(id)
        resp <- chirp match
          case Some(chirp) => Ok(chirp)
          case None => NotFound(FailureResponse("Chirp not found"))
      } yield resp


  private val createChirpRoute: HttpRoutes[F] = HttpRoutes.of[F]:
    case req@POST -> Root / "create" =>
      req.validate[ChirpInfo] { chirpInfo =>
        for {
          chirpId <- chirps.create(UUID.randomUUID(), chirpInfo) // TODO add userId
          resp <- Created(chirpId)
        } yield resp
      }

  private val updateChirpRoute: HttpRoutes[F] = HttpRoutes.of[F]:
    case req@PUT -> Root / UUIDVar(id) =>
      req.validate[ChirpInfo] {
        chirpInfo =>
          for {
            maybeChirp <- chirps.update(id, chirpInfo)
            resp <- maybeChirp match
              case Some(chirp) => Ok()
              case None => NotFound(FailureResponse(s"Cannot update chirp $id: not found"))
          } yield resp
      }


  private val deleteChirpRoute: HttpRoutes[F] = HttpRoutes.of[F]:
    case DELETE -> Root / UUIDVar(id) =>
      chirps.find(id).flatMap:
        case None => NotFound(FailureResponse(s"Chirp $id not found"))
        case Some(_) =>
          for {
            _ <- chirps.delete(id)
            res <- Ok("Chirp deleted")
          } yield res


  val routes: HttpRoutes[F] = Router("/chirps" -> (allChirpsRoute <+> createChirpRoute <+> updateChirpRoute <+> deleteChirpRoute <+> findChirpRoute))

object ChirpRoutes:
  def apply[F[_] : Concurrent : Logger](chirps: Chirps[F]) = new ChirpRoutes[F](chirps)
