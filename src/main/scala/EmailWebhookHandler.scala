import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.{WebhookEmbed, WebhookEmbedBuilder}
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.github.furstenheim.{CopyDown, OptionsBuilder}
import org.apache.commons.mail.util.MimeMessageParser

import java.io.InputStream
import javax.mail.internet.MimeMessage
import scala.language.reflectiveCalls

object EmailWebhookHandler {
  private case class WebhookData(email: InputStream) {
    private val mimeMessageParser = new MimeMessageParser(new MimeMessage(null, email)).parse()
    val title: String = mimeMessageParser.getSubject
    val body: String = if (mimeMessageParser.hasHtmlContent) new CopyDown(OptionsBuilder.anOptions().withBr("").build())
      .convert(mimeMessageParser.getHtmlContent).trim else mimeMessageParser.getPlainContent.trim
  }

  private def getEmailFromS3(bucket: String, key: String): InputStream =
    AmazonS3ClientBuilder.defaultClient().getObject(bucket, key).getObjectContent

  private def sendWebhookEmbedded(webhookData: WebhookData, webhookAddress: String): Unit = {
    val webhookEmbed = new WebhookEmbedBuilder()
      .setColor(0x006EE6)
      .setTitle(new WebhookEmbed.EmbedTitle(webhookData.title, ""))
      .setDescription(webhookData.body)
      .build()

    WebhookClient.withUrl(webhookAddress).send(webhookEmbed).get()
  }

  def handleEvent(s3event: S3Event): Unit = {
    val s3 = s3event.getRecords.get(0).getS3
    sendWebhookEmbedded(WebhookData(getEmailFromS3(s3.getBucket.getName, s3.getObject.getKey)), sys.env("WEBHOOK_ADDRESS"))
  }
}
