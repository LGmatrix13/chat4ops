package utilities

import models.{ ActionRow, Button, IncomingInteraction, Interaction, InteractionResponse, InteractionResponseData, MessageResponse}
import sttp.client4.DefaultSyncBackend
import sttp.client4.quick.*
import upickle.default.write
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.encoders.Hex

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

  def sendAcceptDeclineMessage(channelId: String, content: String): Unit = {
    val backend = DefaultSyncBackend()
    val message = MessageResponse(
      content = content,
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

  def sendInteraction(
     incoming: IncomingInteraction,
     interaction: Interaction
  ): InteractionResponse = {
    val response = InteractionResponseData(
      content = interaction.content(incoming),
      flags = if interaction.ephemeral then Some(64) else null
    )
    val interactionResponse = InteractionResponse(
      `type` = interaction.`type`,
      data = Some(response)
    )
    val backend = DefaultSyncBackend()
    baseRequest
      .post(uri"$rootUrl/interactions/${incoming.id}/${incoming.token}/callback")
      .body(write(interactionResponse))
      .contentType("application/json")
      .send(backend)
    interactionResponse
  }
}
