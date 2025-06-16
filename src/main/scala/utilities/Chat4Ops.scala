package utilities

import models._

object Chat4Ops {
  def executeAction(action: Action): Unit = {
    action match {
      case AcceptDecline(message) =>
        println("Accept decline")
      case Form(inputs) =>
        println("Form")
      case SlashCommand(command) =>
        println("SlashCommand")
    }
  }
}
