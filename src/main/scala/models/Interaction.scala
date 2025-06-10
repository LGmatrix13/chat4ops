package models

import upickle.default.{ReadWriter => RW, macroRW}

case class InteractionRequest(
  `type`: Int,
  token: String,
  id: String
)

object InteractionRequest {
  implicit val rw: RW[InteractionRequest] = macroRW
}


case class InteractionResponse(
  `type`: Int,
  data: InteractionData
)

object InteractionResponse {
  implicit val rw: RW[InteractionResponse] = macroRW
}

case class InteractionData(
  content: String,
  flags: Int
)

object InteractionData {
  implicit val rw: RW[InteractionData] = macroRW
}

