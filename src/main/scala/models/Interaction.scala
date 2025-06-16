package models

import upickle.default.{ReadWriter => RW, macroRW}

case class InteractionRequest (
   `type`: Int,
   token: String,
   id: String,
  data: InteractionRequestData
)

object InteractionRequest {
  implicit val rw: RW[InteractionRequest] = macroRW
}

case class InteractionRequestData(
  custom_id: String
)

object InteractionRequestData {
  implicit val rw: RW[InteractionRequestData] = macroRW
}

case class InteractionResponse(
  `type`: Int,
  data: InteractionResponseData
)

object InteractionResponse {
  implicit val rw: RW[InteractionResponse] = macroRW
}

case class InteractionResponseData(
  content: String,
  flags: Int
)

object InteractionResponseData {
  implicit val rw: RW[InteractionResponseData] = macroRW
}

