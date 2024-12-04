package theproject.domain

import java.util.UUID

object chirps:
  case class Chirp(id: UUID, userId: UUID, content: String, date: Long)
  
  case class ChirpInfo(
      content: String
  )

