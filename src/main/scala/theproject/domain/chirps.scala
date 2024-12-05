package theproject.domain

import java.util.UUID

object chirps:
  case class Chirp(id: UUID, userId: UUID, content: String, date: Long)
  
  case class ChirpInfo(
      content: String
  )
  
  final case class ChirpFilter(
      userId: Option[UUID] = None,
      fromDate: Option[Long] = None,
      toDate: Option[Long] = None
  )

