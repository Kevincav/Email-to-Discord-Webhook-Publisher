package webhook

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.model.StatusCode

import java.io.ByteArrayInputStream
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class WebhookTest extends AnyFlatSpec with BeforeAndAfter with Matchers {
  val inputStreamWithHTML = ByteArrayInputStream("""From: sender@example.com
                                                   |To: recipient@example.com
                                                   |Subject: Multipart Email Example
                                                   |Content-Type: multipart/alternative; boundary="boundary-string"
                                                   |
                                                   |--your-boundary
                                                   |Content-Type: text/plain; charset="utf-8"
                                                   |Content-Transfer-Encoding: quoted-printable
                                                   |Content-Disposition: inline
                                                   |
                                                   |Plain text email goes here!
                                                   |This is the fallback if email client does not support HTML
                                                   |
                                                   |--boundary-string
                                                   |Content-Type: text/html; charset="utf-8"
                                                   |Content-Transfer-Encoding: quoted-printable
                                                   |Content-Disposition: inline
                                                   |
                                                   |<h1>This is the HTML Section!</h1>
                                                   |<p>This is what displays in most modern email clients</p>
                                                   |
                                                   |--boundary-string--""".stripMargin.getBytes)

  val inputStreamText = ByteArrayInputStream("""From: Some One <someone@example.com>
                                               |MIME-Version: 1.0
                                               |Subject: Multipart Email Example
                                               |Content-Type: multipart/mixed;
                                               |        boundary="XXXXboundary text"
                                               |
                                               |This is a multipart message in MIME format.
                                               |
                                               |--XXXXboundary text
                                               |Content-Type: text/plain
                                               |
                                               |this is the body text
                                               |
                                               |--XXXXboundary text
                                               |Content-Type: text/plain;
                                               |Content-Disposition: attachment;
                                               |        filename="test.txt"
                                               |
                                               |this is the attachment text
                                               |
                                               |--XXXXboundary text--""".stripMargin.getBytes)

  it should "test the classes" in {
    WebhookEmbed(Some("Test1")).title.get shouldBe "Test1"
    WebhookEmbed(Some("Test1"), Some("Test2")).description.get shouldBe "Test2"
    WebhookEmbed(Some("Test1"), Some("Test2"), "12345").color shouldBe "12345"
    WebhookEmbed().title shouldBe empty
    WebhookEmbed().description shouldBe empty
    WebhookEmbed().color shouldBe "28390"
    WebhookData(List.empty).embeds shouldBe empty
  }

  "MIME email" should "parse correctly with HTML" in {
    val result = Webhook.parseEmail(inputStreamWithHTML)
    result.title.get shouldBe "Multipart Email Example"
    result.description.get shouldBe s"This is the HTML Section!\n=========================\n\nThis is what displays in most modern email clients"
  }

  "MIME email" should "parses correctly without HTML" in {
    val result = Webhook.parseEmail(inputStreamText)
    result.title.get shouldBe "Multipart Email Example"
    result.description.get shouldBe "this is the body text"
  }

  "webhook class" should "publish a webhook request" in {
    val wireMockServer: WireMockServer = WireMockServer(10001)
    wireMockServer.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse.withStatus(200)))
    wireMockServer.start()
    val result = Await.result(Webhook.publish("http://localhost:10001/webhook", WebhookData(List(WebhookEmbed(Some("Test Title"), Some("Test Description"))))), 2.seconds)
    wireMockServer.stop()
    result.code shouldBe StatusCode(200)
  }
}
