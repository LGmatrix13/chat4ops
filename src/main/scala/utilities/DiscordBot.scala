package utilities

import models.{ActionRow, Button, Interaction, MessageResponse}
import sttp.client4.DefaultSyncBackend
import sttp.client4.quick.*
import upickle.default.write

object DiscordBot {
  private final val rootUrl = "https://discord.com/api/v10"
  private final val applicationId = EnvLoader.get("DISCORD_APPLICATION_ID")
  private final val botToken = EnvLoader.get("DISCORD_BOT_TOKEN")
  private final val url = EnvLoader.get("DISCORD_BOT_URL")
  private final val versionNumber = 1.0

  private def baseRequest = basicRequest
    .header("Authorization", s"Bot $botToken")
    .header("User-Agent", s"DiscordBot ($url, $versionNumber)")
    .header("Content-Type", "application/json")

  def sendAcceptDeclineMessage(channelId: String): Unit = {
    val backend = DefaultSyncBackend()
    val message = MessageResponse(
      content = "Click the button below!",
      components = List(
        ActionRow(
          `type` = 1,
          components = List(
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

  def sendAcceptDeclineInteraction(channelId: String, interactionToken: String): Unit = {
    val backend = DefaultSyncBackend()
    val interaction = Interaction(
      `type` = 4
    )
    val jsonString: String = write(interaction)
    val response = baseRequest
      .post(uri"$rootUrl/interactions/$channelId/$interactionToken/callback")
      .body(jsonString)
      .send(backend)
  }
}
