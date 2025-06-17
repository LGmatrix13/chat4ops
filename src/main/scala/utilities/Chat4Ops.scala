package utilities

import enums.InteractionType.{AcceptDecline, Form, Slash}
import models.*

import scala.collection.parallel.CollectionConverters.*

object Chat4Ops {
  def executeActions(actions: Seq[Action]): Boolean = {
    actions.par.map(action => this.executeAction(action)).forall(_ == true)
  }

  def executeAction(action: Action): Boolean = {
    action match {
      case AcceptDeclineAction(message, channelId) =>
        DiscordBot.sendAcceptDeclineMessage(channelId = channelId)
        true
      case FormAction(inputs, channelId) =>
        println(s"handle form with channelId $channelId")
        true
      case _ => false
    }
  }

  def executeInteraction(incomingInteraction: IncomingInteraction, interactions: Interactions): Boolean = {
    incomingInteraction.`type` match {
      case AcceptDecline.value if interactions.acceptDeclineInteraction.isDefined =>
        DiscordBot.sendInteraction(
          incoming = incomingInteraction,
          interaction = interactions.acceptDeclineInteraction.get,
        )
        true
      case Slash.value if interactions.slashInteraction.isDefined =>
        DiscordBot.sendInteraction(
          incoming = incomingInteraction,
          interaction = interactions.slashInteraction.get,
        )
        true
      case Form.value if interactions.formInteraction.isDefined =>
        DiscordBot.sendInteraction(
          incoming = incomingInteraction,
          interaction = interactions.formInteraction.get
        )
        true
      case _ => false
    }
  }
}
