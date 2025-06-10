import models.InteractionRequest
import sttp.shared.Identity
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import utilities.{DiscordBot, EnvLoader}
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.encoders.Hex
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import sttp.model.StatusCode
import io.circe.generic.auto.*
import upickle.default.read

sealed trait ErrorInfo
case class NotFound() extends ErrorInfo
case class Unauthorized() extends ErrorInfo
case class BadRequest() extends ErrorInfo
case object NoContent extends ErrorInfo

val baseEndpoint: Endpoint[Unit, Unit, ErrorInfo, Unit, Any] = endpoint.errorOut(
  oneOf[ErrorInfo](
    oneOfVariant(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
    oneOfVariant(StatusCode.Unauthorized, jsonBody[Unauthorized].description("unauthorized")),
    oneOfVariant(StatusCode.BadRequest, jsonBody[BadRequest].description("bad request")),
    oneOfVariant(StatusCode.NoContent, emptyOutputAs(NoContent)),
  )
)

@main def main(): Unit =
  EnvLoader.loadEnv("./src/.env")

  val sendEndpoint = baseEndpoint
    .get
    .in("api" / "send")
    .out(stringBody)
    .handleSuccess(_ => {
      val channelId = "1381992880834351184"
      DiscordBot.sendAcceptDeclineMessage(channelId)
      "Success"
    })

  val discordPublicKey = EnvLoader.get("DISCORD_PUBLIC_KEY")
  def verifySignature(
     publicKey: String,
     signature: String,
     timestamp: String,
     body: String
  ): Boolean = {

    val publicKeyBytes = Hex.decode(publicKey.strip())
    val signatureBytes = Hex.decode(signature.strip())
    val message = (timestamp.strip() + body.strip()).getBytes("UTF-8")
    val verifier = new Ed25519Signer()
    verifier.init(false, new Ed25519PublicKeyParameters(publicKeyBytes, 0))
    verifier.update(message, 0, message.length)
    verifier.verifySignature(signatureBytes)
  }

  val respondEndpoint = baseEndpoint
    .post
    .in("api" / "interactions")
    .in(header[String]("X-Signature-Ed25519"))
    .in(header[String]("X-Signature-Timestamp"))
    .in(stringBody)
    .out(stringBody)
    .handle { case (signature, timestamp, body) =>
      val isValid = verifySignature(
        discordPublicKey,
        signature,
        timestamp,
        body
      )
      if (!isValid) {
        Left(Unauthorized())
      } else {
        val interaction = read[InteractionRequest](body)
        interaction.`type` match {
          case 1 =>
            Right("Success")
          case 2 =>
            DiscordBot.sendAcceptDeclineInteraction(
              interactionId = interaction.id,
              interactionToken = interaction.token
            )
            Right("Success")
          case 3 =>
            DiscordBot.sendAcceptDeclineInteraction(
              interactionId = interaction.id,
              interactionToken = interaction.token
            )
            Right("Success")
          case _ =>
            Left(BadRequest())
        }
      }
    }

  val endpoints = List(sendEndpoint, respondEndpoint)
  val swaggerEndpoints = SwaggerInterpreter()
    .fromServerEndpoints[Identity](endpoints, "Chat4Ops", "1.0")

  NettySyncServer()
    .port(8080)
    .addEndpoints(endpoints)
    .addEndpoints(swaggerEndpoints)
    .startAndWait()