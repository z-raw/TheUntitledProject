package theproject.http.validation

import cats.MonadThrow
import cats.data.Validated
import cats.data.Validated.*
import cats.implicits.*
import cats.syntax.all.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.{EntityDecoder, Request, Response}
import org.typelevel.log4cats.Logger
import theproject.http.responses.FailureResponse
import validators.*
import theproject.logging.syntax.*

object syntax {

  private def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] =
    validator.validate(entity)

  trait HttpValidationDsl[F[_] : MonadThrow : Logger] extends Http4sDsl[F] {

    extension (req: Request[F])
      def validate[A: Validator](continuationIfValid: A => F[Response[F]])(
        using EntityDecoder[F, A]): F[Response[F]] = {

        req
          .as[A]
          .logError(e => s"Failed to decode request body: ${e.getMessage}")
          .map(validateEntity)
          .flatMap {
            case Valid(a) => continuationIfValid(a)
            case Invalid(errors) =>
              val errorMessages = errors.toList.map(_.errorMessage).mkString(", ")
              BadRequest(FailureResponse(s"Invalid request: $errorMessages"))
          }
      }
  }
}
