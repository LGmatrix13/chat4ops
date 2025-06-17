package models

import upickle.default.{macroRW, ReadWriter as RW}

case class IncomingInteraction(
  `type`: Int,
  token: String,
  id: String,
  data: Option[IncomingInteractionData] = null,
)
object IncomingInteraction {
  implicit val rw: RW[IncomingInteraction] = macroRW
}

case class IncomingInteractionData(
  `type`: Int,
  custom_id: Option[String] = null
)
object IncomingInteractionData {
  implicit val rw: RW[IncomingInteractionData] = macroRW
}

trait Interaction:
  def `type`: Int
  def ephemeral: Boolean
  def content(incoming: IncomingInteraction): String
case class SlashInteraction(
   message: String,
   ephemeral: Boolean
) extends Interaction:
  override val `type`: Int = 2
  override def content(incoming: IncomingInteraction): String = message
object SlashInteraction {
  implicit val rw: RW[SlashInteraction] = macroRW
}

case class Interactions(
  acceptDeclineInteraction: Option[AcceptDeclineInteraction] = null,
  slashInteraction: Option[SlashInteraction] = null,
  formInteraction: Option[FormInteraction] = null
)

case class AcceptDeclineInteraction(
 decliningMessage: String,
 acceptingMessage: String,
 ephemeral: Boolean
) extends Interaction:
  override val `type`: Int = 3
  override def content(incoming: IncomingInteraction): String =
    incoming.data.get.custom_id match
      case Some("accept") => acceptingMessage
      case _ => decliningMessage
object AcceptDeclineInteraction {
  implicit val rw: RW[AcceptDeclineInteraction] = macroRW
}

case class FormInteraction(
  message: String,
  ephemeral: Boolean
) extends Interaction:
  override val `type`: Int = 5
  override def content(incoming: IncomingInteraction): String = message
object FormInteraction {
  implicit val rw: RW[FormInteraction] = macroRW
}

case class InteractionResponse(
  `type`: Int,
  data: Option[InteractionResponseData] = null
)
object InteractionResponse {
  implicit val rw: RW[InteractionResponse] = macroRW
}

case class InteractionResponseData(
  content: String,
  flags: Option[Int] = null
)
object InteractionResponseData {
  implicit val rw: RW[InteractionResponseData] = macroRW
}
