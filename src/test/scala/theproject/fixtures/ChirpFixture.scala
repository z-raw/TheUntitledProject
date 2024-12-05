package theproject.fixtures

import theproject.domain.chirps.{Chirp, ChirpInfo}

import java.util.UUID

trait ChirpFixture {
  
  val ValidChirpUUID = UUID.fromString("30b8119b-7893-4436-9504-8f1342971a42")
  
  val InvalidChirpUUID = UUID.fromString("30b8119b-7893-4436-9504-8f1342971a43")
  
  val NewChirpUUID = UUID.fromString("30b8119b-7893-4436-9504-8f1342971a44")

  val NonExistentChirpUUID = UUID.fromString("00000000-0000-0000-0000-000000000003")

  val User1UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
  
  val User2UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
  
  val InvalidChirp = Chirp(InvalidChirpUUID, null, "This is an invalid chirp with user id NULL", 0L)
  
  val ValidChirp = Chirp(ValidChirpUUID, User1UUID, "This is a valid chirp", 42L)
  
  val UpdatedChirp = Chirp(ValidChirpUUID, User1UUID, "This is an updated chirp", 42L)
  
  val ValidChirpInfo = ChirpInfo("This is a valid chirp")
  
  val UpdatedChirpInfo = ChirpInfo("This is an updated chirp")
  
  val NewChirpInfo = ChirpInfo("This is a new chirp")
}
