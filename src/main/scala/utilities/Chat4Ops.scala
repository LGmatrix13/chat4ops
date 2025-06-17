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
    (incomingInteraction.`type`, interactions) match {
      case (AcceptDecline.value, Interactions(Some(adi), _, _)) =>
        DiscordBot.sendAcceptDeclineInteraction(incomingInteraction, adi)
        true
      case (Slash.value, Interactions(_, Some(slash), _)) =>
        DiscordBot.sendSlashInteraction(incomingInteraction, slash)
        true
      case (Form.value, Interactions(_, _, Some(form))) =>
        DiscordBot.sendFormInteraction(incomingInteraction, form)
        true
      case _ => false
    }
  }

}
