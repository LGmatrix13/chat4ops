package models

import upickle.default.{ReadWriter => RW, macroRW}

case class InteractionRequest(
  `type`: Int,
  token: String,
  channel_id: String
)

object InteractionRequest {
  implicit val rw: RW[InteractionRequest] = macroRW
}


case class Interaction(
  `type`: Int,
)

object Interaction {
  implicit val rw: RW[Interaction] = macroRW
}
