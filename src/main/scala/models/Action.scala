package models

trait Action

case class AcceptDecline(
  message: String
) extends Action

case class Form(
  inputs: List[Input]
) extends Action

case class SlashCommand(
  command: String
) extends Action