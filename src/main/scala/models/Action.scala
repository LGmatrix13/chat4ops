package models

trait Action:
  val channelId: String

case class AcceptDecline(
    channelId: String,
    message: String
) extends Action

case class Form(
   channelId: String,
   inputs: Seq[Input]
) extends Action

case class SlashCommand(
   channelId: String,
   command: String
) extends Action