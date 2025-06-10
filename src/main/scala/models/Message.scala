package models

import upickle.default.ReadWriter

case class Message(
  content: String,
  components: List[ActionRow]
)

case class ActionRow(
  `type`: Int = 1,
  components: List[Button]
)

case class Button(
 `type`: Int = 2,
 style: Int,
 label: String,
 custom_id: String
)

object Message {
  implicit val rw: RW[Message] = macroRW
} 