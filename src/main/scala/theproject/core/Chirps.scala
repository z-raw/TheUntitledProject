package theproject.core

import cats.Monad
import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*
import org.typelevel.log4cats.Logger
import theproject.domain.*
import theproject.domain.chirps.*
import theproject.domain.pagination.*
import theproject.logging.syntax.*

import java.util.UUID

trait Chirps[F[_]] {

  def all(): F[List[Chirp]]

  def all(filter: ChirpFilter, pagination: Pagination): F[List[Chirp]]

  def create(userId: UUID, chirpInfo: ChirpInfo): F[UUID]

  def find(id: UUID): F[Option[Chirp]]

  def update(id: UUID, chirpInfo: ChirpInfo): F[Option[Chirp]]

  def delete(id: UUID): F[Int]

}

class LiveChirps[F[_] : MonadCancelThrow: Logger] private(xa: Transactor[F]) extends Chirps[F] {

  def all(): F[List[Chirp]] =
    sql"""
         |SELECT id, user_id, content, created_at
         |FROM chirps
         |""".stripMargin
      .query[Chirp]
      .to[List]
      .transact(xa)

  def all(filter: ChirpFilter, pagination: Pagination): F[List[Chirp]] =

    val selectFr =
      fr"""
         |SELECT id,
         | user_id,
         |  content,
         |   created_at
         |""".stripMargin

    val fromFr = fr"FROM chirps"

    val whereFr = Fragments.whereAndOpt(
      filter.userId.map(userId => fr"user_id = $userId"),
      filter.fromDate.map(fromDate => fr"created_at >= $fromDate"),
      filter.toDate.map(toDate => fr"created_at <= $toDate")
    )

    val limitOffsetFr = fr"ORDER BY created_at DESC LIMIT ${pagination.limit} OFFSET ${pagination.offset}"

    val statement = selectFr |+| fromFr |+| whereFr |+| limitOffsetFr

    statement
      .query[Chirp]
      .to[List]
      .transact(xa)

  def create(userId: UUID, chirpInfo: ChirpInfo): F[UUID] = {
    sql"""
         |INSERT INTO chirps 
         |(id,
         | user_id,
         | content,
         | created_at)
         |   
         |   VALUES
         |   (${UUID.randomUUID()},
         |   $userId,
         |   ${chirpInfo.content},
         |   ${System.currentTimeMillis()})
         |""".stripMargin
      .update
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)
  }

  def find(id: UUID): F[Option[Chirp]] =
    sql"""
         |SELECT id, user_id, content, created_at
         |FROM chirps
         |WHERE id = $id
         |""".stripMargin
      .query[Chirp]
      .option
      .transact(xa).log(s=>s"Found chirp:$s", e => s"Error finding chirp $id: $e")

  def update(id: UUID, chirpInfo: ChirpInfo): F[Option[Chirp]] =
    sql"""
         |UPDATE chirps
         |SET content = ${chirpInfo.content}
         |WHERE id = $id
         |""".stripMargin
      .update
      .run
      .transact(xa)
      .flatMap(_ => find(id))

  def delete(id: UUID): F[Int] =
    sql"""
         |DELETE FROM chirps
         |WHERE id = $id
         |""".stripMargin
      .update
      .run
      .transact(xa)
    

}

object LiveChirps {

  given chirpRead: Read[Chirp] = Read[
    (UUID, UUID, String, Long)]
    .map {
      case (id, userId, content, createdAt) => Chirp(id, userId, content, createdAt)
    }

  def apply[F[_] : MonadCancelThrow: Logger](xa: Transactor[F]): F[LiveChirps[F]] = Monad[F].pure(new LiveChirps[F](xa))

}