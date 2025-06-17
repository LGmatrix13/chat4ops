package models

import upickle.default.{ReadWriter => RW, macroRW}

case class MessageResponse(
  content: String,
  components: Seq[ActionRow]
)

object MessageResponse {
  implicit val rw: RW[MessageResponse] = macroRW
}

case class ActionRow(
  `type`: Int,
  components: Seq[Button]
)

object ActionRow {
  implicit val rw: RW[ActionRow] = macroRW
}

case class Button(
 `type`: Int,
 style: Int,
 label: String,
 custom_id: String,
)

object Button {
  implicit val rw: RW[Button] = macroRW
}