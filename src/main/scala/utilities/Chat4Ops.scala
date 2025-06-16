package utilities

import models._
import scala.collection.parallel.CollectionConverters._

object Chat4Ops {
  def executeActions(actions: Seq[Action]): Boolean = {
    actions.par.map(action => this.executeAction(action)).forall(_ == true)
  }

  def executeAction(action: Action): Boolean = {
    action match {
      case AcceptDecline(message, channelId) =>
        DiscordBot.sendAcceptDeclineMessage(channelId = channelId)
        true
      case Form(inputs, channelId) =>
        println(s"handle form with channelId $channelId")
        true
      case SlashCommand(command, channelId) =>
        println(s"handle form with channelId $channelId")
        true
      case _ => false
    }
  }
  
  def handleInteraction(): Boolean = {
    
  }
}
