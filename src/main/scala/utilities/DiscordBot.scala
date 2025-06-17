package utilities

import enums.InteractionType
import models.{AcceptDeclineInteraction, ActionRow, Button, FormInteraction, IncomingInteraction, Interaction, InteractionResponse, InteractionResponseData, MessageResponse, SlashInteraction}
import sttp.client4.DefaultSyncBackend
import sttp.client4.quick.*
import upickle.default.write
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.encoders.Hex
import sttp.model.StatusCode

object DiscordBot {
  private final val rootUrl = "https://discord.com/api/v10"
  private final val applicationId = EnvLoader.get("DISCORD_APPLICATION_ID")
  private final val botToken = EnvLoader.get("DISCORD_BOT_TOKEN")
  private final val url = EnvLoader.get("DISCORD_BOT_URL")
  private final val versionNumber = 1.0
  private val discordPublicKey = EnvLoader.get("DISCORD_PUBLIC_KEY")

  private def baseRequest = basicRequest
    .header("Authorization", s"Bot $botToken")
    .header("User-Agent", s"DiscordBot ($url, $versionNumber)")
    .header("Content-Type", "application/json")


  def verifySignature(
   signature: String,
   timestamp: String,
   body: String
 ): Boolean = {
    val publicKeyBytes = Hex.decode(discordPublicKey.strip())
    val signatureBytes = Hex.decode(signature.strip())
    val message = (timestamp.strip() + body.strip()).getBytes("UTF-8")
    val verifier = new Ed25519Signer()
    verifier.init(false, new Ed25519PublicKeyParameters(publicKeyBytes, 0))
    verifier.update(message, 0, message.length)
    verifier.verifySignature(signatureBytes)
  }


  def sendAcceptDeclineMessage(channelId: String): Unit = {
    val backend = DefaultSyncBackend()
    val message = MessageResponse(
      content = "Click the button below!",
      components = Seq(
        ActionRow(
          `type` = 1,
          components = Seq(
            Button(
              `type` = 2,
              style = 1,
              label = "Accept",
              custom_id = "1"
            ),
            Button(
              `type` = 2,
              style = 2,
              label = "Decline",
              custom_id = "2"
            )
          )
        )
      )
    )
    val jsonString: String = write(message)
    val response = baseRequest
      .post(uri"$rootUrl/channels/$channelId/messages")
      .body(jsonString)
      .send(backend)
  }

  private def sendInteraction(incomingInteraction: IncomingInteraction, interaction: Interaction, interactionResponseData: InteractionResponseData): Unit = {
    val backend = DefaultSyncBackend()
    val interactionResponse = InteractionResponse(
      `type` = interaction.`type`,
      data = interactionResponseData
    )
    val jsonString: String = write(interactionResponse)
    val response = baseRequest
      .post(uri"$rootUrl/interactions/${incomingInteraction.id}/${incomingInteraction.token}/callback")
      .body(jsonString)
      .send(backend)
  }

  def sendAcceptDeclineInteraction(incomingInteraction: IncomingInteraction, acceptDeclineInteraction: AcceptDeclineInteraction): Unit = {
    val customId = incomingInteraction.data.custom_id.get
    sendInteraction(
      incomingInteraction = incomingInteraction,
      interaction = acceptDeclineInteraction,
      interactionResponseData = InteractionResponseData(
        content = if customId == "accept" then acceptDeclineInteraction.acceptingMessage else acceptDeclineInteraction.decliningMessage,
        flags = if acceptDeclineInteraction.ephemeral then Some(64) else null
      )
    )
  }

  def sendSlashInteraction(incomingInteraction: IncomingInteraction, slashInteraction: SlashInteraction): Unit = {
    sendInteraction(
      incomingInteraction = incomingInteraction,
      interaction = slashInteraction,
      interactionResponseData = InteractionResponseData(
        content = slashInteraction.message,
        flags = if slashInteraction.ephemeral then Some(64) else null
      )
    )
  }

  def sendFormInteraction(incomingInteraction: IncomingInteraction, formInteraction: FormInteraction): Unit = {
    sendInteraction(
      incomingInteraction = incomingInteraction,
      interaction = formInteraction,
      interactionResponseData = InteractionResponseData(
        content = formInteraction.message,
        flags = if formInteraction.ephemeral then Some(64) else null
      )
    )
  }
}
