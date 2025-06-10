package models

import upickle.default.{ReadWriter => RW, macroRW}

case class Interaction(
  `type`: Int,
  data: Option[InteractionData] = None
)

object Interaction {
  implicit val rw: RW[Interaction] = macroRW
}

case class InteractionData(
   content: String,
   flag: Int
)

object InteractionData {
  implicit val rw: RW[InteractionData] = macroRW
}