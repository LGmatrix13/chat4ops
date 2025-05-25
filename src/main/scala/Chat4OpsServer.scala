import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.stream.{ActorMaterializer, Materializer, SystemMaterializer}
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import scala.io.StdIn
import scala.concurrent.ExecutionContextExecutor

object Chat4OpsServer {
  implicit val system: ActorSystem = ActorSystem("chat4ops")
  val materializer: Materializer = SystemMaterializer(system).materializer
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]): Unit = {
    val route =
      path("api" / "messages") {
        post {
          entity(as[String]) { body =>
            parse(body) match {
              case Left(parsingError) =>
                complete(HttpResponse(StatusCodes.BadRequest, entity = s"Invalid JSON: ${parsingError.getMessage}"))

              case Right(json) =>
                println("Received payload:")
                println(json.spaces2)

                val adaptiveCardResponse: Json = Json.obj(
                  "type" -> Json.fromString("message"),
                  "attachments" -> Json.arr(
                    Json.obj(
                      "contentType" -> Json.fromString("application/vnd.microsoft.card.adaptive"),
                      "content" -> Json.obj(
                        "$schema" -> Json.fromString("http://adaptivecards.io/schemas/adaptive-card.json"),
                        "type" -> Json.fromString("AdaptiveCard"),
                        "version" -> Json.fromString("1.3"),
                        "body" -> Json.arr(
                          Json.obj(
                            "type" -> Json.fromString("TextBlock"),
                            "text" -> Json.fromString("Do you want to proceed?")
                          )
                        ),
                        "actions" -> Json.arr(
                          Json.obj(
                            "type" -> Json.fromString("Action.Submit"),
                            "title" -> Json.fromString("Yes"),
                            "data" -> Json.obj("choice" -> Json.fromString("yes"))
                          ),
                          Json.obj(
                            "type" -> Json.fromString("Action.Submit"),
                            "title" -> Json.fromString("No"),
                            "data" -> Json.obj("choice" -> Json.fromString("no"))
                          )
                        )
                      )
                    )
                  )
                )

                complete(
                  HttpResponse(
                    status = StatusCodes.OK,
                    entity = HttpEntity(ContentTypes.`application/json`, adaptiveCardResponse.spaces2)
                  )
                )
            }
          }
        }
      }

    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(route)

    println("Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
