package webhook

import io.github.furstenheim.{CopyDown, OptionsBuilder}
import org.apache.commons.mail.util.MimeMessageParser
import play.api.libs.json.{Json, Writes}
import sttp.client4.{DefaultFutureBackend, Response, UriContext, quickRequest}

import java.io.InputStream
import javax.mail.internet.MimeMessage
import scala.concurrent.Future

case class WebhookEmbed(title: Option[String] = None, description: Option[String] = None, color: String = sys.env.getOrElse("EMBED_COLOR", "28390"))
case class WebhookData(embeds: List[WebhookEmbed])

object Webhook {
  implicit val webhookEmbedFormat: Writes[WebhookEmbed] = Json.writes[WebhookEmbed]
  implicit val webhookDataFormat: Writes[WebhookData] = Json.writes[WebhookData]

  def parseEmail(email: InputStream, color: String = "28390"): WebhookEmbed = {
    val mimeMessageParser = new MimeMessageParser(new MimeMessage(null, email)).parse()
    val copyDown = new CopyDown(OptionsBuilder.anOptions().withBr("").build())
    val body: String = if (mimeMessageParser.hasHtmlContent) mimeMessageParser.getHtmlContent else mimeMessageParser.getPlainContent
    WebhookEmbed(Some(mimeMessageParser.getSubject), Some(copyDown.convert(body).trim), color)
  }

  def publish(webhookAddress: String, webhookData: WebhookData): Future[Response[String]] = quickRequest.header("Content-Type",
    "application/json").body(Json.toJson(webhookData).toString).post(uri"$webhookAddress").send(DefaultFutureBackend())
}
