package theproject.http.routes

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import theproject.core.Chirps
import theproject.domain.chirps.*
import theproject.domain.pagination
import theproject.domain.pagination.Pagination
import theproject.fixtures.ChirpFixture
import theproject.http.responses.FailureResponse

import java.util.UUID


class ChirpRoutesSpec
  extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with ChirpFixture {

  ///////////////////
  //// prep
  ///////////////////

  val chirps: Chirps[IO] = new Chirps[IO]:

    override def all(): IO[List[Chirp]] =
      IO(List(ValidChirp))

    override def all(filter: ChirpFilter, pagination: Pagination): IO[List[Chirp]] =
      IO(List(ValidChirp))

    override def create(userId: UUID, chirpInfo: ChirpInfo): IO[UUID] =
      IO(ValidChirpUUID)

    override def find(id: UUID): IO[Option[Chirp]] =
      if (id == ValidChirpUUID) IO.pure(Some(ValidChirp))
      else IO.pure(None)

    override def update(id: UUID, chirpInfo: ChirpInfo): IO[Option[Chirp]] =
      if (id == ValidChirpUUID) IO.pure(Some(UpdatedChirp))
      else IO.pure(None)

    override def delete(id: UUID): IO[Int] =
      if (id == ValidChirpUUID) IO.pure(1)
      else IO.pure(0)

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val chirpRoutes = ChirpRoutes[IO](chirps).routes


  ///////////////////
  //// tests
  ///////////////////

  "ChirpRoutes" - {
    "should return all chirps" in {

      for {
        response <- chirpRoutes.orNotFound.run(
          Request(method = POST, uri = uri"/chirps").withEntity(ChirpFilter())
        )
        retreived <- response.as[List[Chirp]]

      } yield {
        response.status mustBe Ok
        retreived mustBe List(ValidChirp)
      }
    }
    
    "should return a chirp with a valid id" in {

      for {
        response <- chirpRoutes.orNotFound.run(
          Request(method = GET, uri = uri"/chirps/30b8119b-7893-4436-9504-8f1342971a42")
        )
        retreived <- response.as[Chirp]

      } yield {
        response.status mustBe Ok
        retreived mustBe ValidChirp
      }
    }
    
    "should return a chirp not found with an invalid id" in {

      for {
        response <- chirpRoutes.orNotFound.run(
          Request(method = GET, uri = uri"/chirps/30b8119b-7893-4436-9504-8f1342971a43")
        )
        retreived <- response.as[FailureResponse]

      } yield {
        response.status mustBe NotFound
        retreived mustBe FailureResponse("Chirp not found")
      }
    }
    
    "should create a new chirp" in {

      for {
        response <- chirpRoutes.orNotFound.run(
          Request(method = POST, uri = uri"/chirps/create").withEntity(ValidChirpInfo)
        )
        retreived <- response.as[UUID]

      } yield {
        response.status mustBe Created
        retreived mustBe ValidChirpUUID
      }
    }
    
    "should update a chirp with a valid id" in {

      for {
        responseValid <- chirpRoutes.orNotFound.run(
          Request(method = PUT, uri = uri"/chirps/30b8119b-7893-4436-9504-8f1342971a42").withEntity(UpdatedChirpInfo)
        )
        responseInvalid <- chirpRoutes.orNotFound.run(
          Request(method = PUT, uri = uri"/chirps/00000000-0000-0000-0000-000000000003").withEntity(UpdatedChirpInfo)
        )

      } yield {
        responseValid.status mustBe Ok
        responseInvalid.status mustBe NotFound
      }
    }
  }
  
  "should delete a chirp with a valid id" in {

    for {
      responseValid <- chirpRoutes.orNotFound.run(
        Request(method = DELETE, uri = uri"/chirps/30b8119b-7893-4436-9504-8f1342971a42")
      )
      responseInvalid <- chirpRoutes.orNotFound.run(
        Request(method = DELETE, uri = uri"/chirps/00000000-0000-0000-0000-000000000003")
      )

    } yield {
      responseValid.status mustBe Ok
      responseInvalid.status mustBe NotFound
    }
  }

  "should filter chirps by limit and offset" in {

    for {
      response <- chirpRoutes.orNotFound.run(
        Request(method = POST, uri = uri"/chirps?limit=10&offset=0")
      )
      retreived <- response.as[List[Chirp]]

    } yield {
      response.status mustBe Ok
      retreived.size mustBe <= (10)
      retreived mustBe List(ValidChirp)
    }
  }

  "should filter chirps by toDate" in {

    for {
      response <- chirpRoutes.orNotFound.run(
        Request(method = POST, uri = uri"/chirps?toDate=40")
      )
      retreived <- response.as[List[Chirp]]

    } yield {
      response.status mustBe Ok
      retreived mustBe List.empty
    }
  }

  "should filter chirps by fromDate" in {

    for {
      response <- chirpRoutes.orNotFound.run(
        Request(method = POST, uri = uri"/chirps?fromDate=40")
      )
      retreived <- response.as[List[Chirp]]

    } yield {
      response.status mustBe Ok
      retreived mustBe List(ValidChirp)
    }
  }

  "should filter chirps by valid userId" in {

    for {
      response <- chirpRoutes.orNotFound.run(
        Request(method = POST, uri = uri"/chirps?userId=00000000-0000-0000-0000-000000000001")
      )
      retreived <- response.as[List[Chirp]]

    } yield {
      response.status mustBe Ok
      retreived mustBe List(ValidChirp)
    }
  }

  "should filter chirps by invalid userId" in {

    for {
      response <- chirpRoutes.orNotFound.run(
        Request(method = POST, uri = uri"/chirps?userId=00000000-0000-0000-0000-000000000002")
      )
      retreived <- response.as[List[Chirp]]

    } yield {
      response.status mustBe Ok
      retreived mustBe List.empty
    }
  }
}