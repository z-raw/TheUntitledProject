package theproject.core

import cats.effect.*
import cats.implicits.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import theproject.domain.chirps.{Chirp, ChirpFilter, ChirpInfo}
import theproject.domain.pagination.Pagination
import theproject.fixtures.ChirpFixture

import java.util.UUID

class ChirpsSpec
  extends AsyncFreeSpec
    with DoobieSpec
    with AsyncIOSpec
    with Matchers
    with ChirpFixture {

  val initScript = "sql/chirps.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]


  "Chirps" - {
    "should return all chirps if table is not empty" in {
      transactor.use { xa =>
        val program = for {
          chirps <- LiveChirps[IO](xa)
          result <- chirps.all(ChirpFilter(), Pagination.default)
        } yield result

        program.asserting(_ shouldBe List(ValidChirp))
      }
    }


    "should create a new chirp" in {
      transactor.use { xa =>
        val program = for {
          chirps <- LiveChirps[IO](xa)
          id <- chirps.create(User1UUID, NewChirpInfo)
          result <- chirps.find(id)
        } yield result

        program.asserting(_.map(_.content) shouldBe Some(NewChirpInfo.content))
      }
    }

    "should find a chirp by valid id" in {
      transactor.use { xa =>
        val program = for {
          chirps <- LiveChirps[IO](xa)
          result <- chirps.find(ValidChirpUUID)
        } yield result

        program.asserting(_.map(_.content) shouldBe Some(ValidChirp.content))
      }
    }

    "should update a chirp" in {
      transactor.use { xa =>
        val program = for {
          chirps <- LiveChirps[IO](xa)
          _ <- chirps.update(ValidChirpUUID, UpdatedChirpInfo)
          result <- chirps.find(ValidChirpUUID)
        } yield result

        program.asserting(_.map(_.content) shouldBe Some(UpdatedChirpInfo.content))
      }
    }

    "should delete a chirp" in {
      transactor.use { xa =>
        val program = for {
          chirps <- LiveChirps[IO](xa)
          id <- chirps.create(UUID.randomUUID(), ChirpInfo("To be deleted"))
          _ <- chirps.delete(id)
          result <- chirps.find(id)
        } yield result

        program.asserting(_ shouldBe None)
      }
    }
  }

  "should return none when chirps table is empty" in {
    transactor.use { xa =>
      val program = for {
        chirps <- LiveChirps[IO](xa)
        _ <- chirps.delete(ValidChirpUUID) // delete the only chirp
        result <- chirps.all(ChirpFilter(), Pagination.default)
      } yield result

      program.asserting(_ shouldBe List.empty)
    }
  }

}
