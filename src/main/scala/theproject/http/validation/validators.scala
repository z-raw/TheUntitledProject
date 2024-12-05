package theproject.http.validation

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.syntax.all.*
import cats.syntax.apply.*
import cats.data.*
import cats.data.Validated.*
import theproject.domain.chirps.ChirpInfo

object validators {

  sealed trait ValidationFailure(val errorMessage: String)
  case class MissingField(fieldName: String) extends ValidationFailure(s"Field $fieldName is missing")
  case class InvalidField(fieldName: String, reason: String) extends ValidationFailure(s"Field $fieldName is invalid: $reason")

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(value: A) : ValidationResult[A]

  }

  def validateRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] =
    if (required(field)) field.validNel
    else MissingField(fieldName).invalidNel

  given chirpInfoValidator: Validator[ChirpInfo] = (chirpInfo : ChirpInfo) => {

    val ChirpInfo(content) = chirpInfo
    
    val validContent = validateRequired(content,"content")(_.nonEmpty)

    (
      validContent
    ).map(ChirpInfo.apply)
  }


}
