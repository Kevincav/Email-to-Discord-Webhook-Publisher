import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.{RequestParametersEntity, ResponseElementsEntity, S3BucketEntity, S3Entity, S3EventNotificationRecord, S3ObjectEntity, UserIdentityEntity}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse}
import sttp.model.StatusCode

import java.io.ByteArrayInputStream
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*

class WebhookLambdaTest extends AnyFlatSpec with Matchers with MockitoSugar {
  val inputStreamText: String = """From: Some One <someone@example.com>
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
                                  |--XXXXboundary text--""".stripMargin

  val s3EventNotificationRecord = S3EventNotificationRecord("us-east-1", "ObjectCreated:Put", "aws:s3", "1970-01-01T00:00:00.000Z",
    "2.0", RequestParametersEntity("127.0.0.1"), ResponseElementsEntity("IOWQ4fDEXAMPLEQM+ey7N9WgVhSnQ6JEXAMPLEZb7hSQDASK+Jd1vEXAMPLEa3Km",
      "79104EXAMPLEB723"), S3Entity("testConfigRule", S3BucketEntity("testbucket", UserIdentityEntity("EXAMPLE"), "arn:aws:s3:::testbucket"),
      S3ObjectEntity("testkey", 5065717, "c2d226b2e97bec9265eb7e59d2dfac41", "2.0", ""), "s3SchemaVersion"), UserIdentityEntity("EXAMPLE"))

  "handle request" should "publish a webhook upon call" in {
    val mockS3Client = mock[S3Client]
    val mockContext = mock[Context]
    when(mockS3Client.getObject(GetObjectRequest.builder().bucket("testbucket").key("testkey").build))
      .thenReturn(ResponseInputStream(GetObjectResponse.builder.build, ByteArrayInputStream(inputStreamText.getBytes)))

    val wireMockServer: WireMockServer = WireMockServer(10000)
    wireMockServer.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse.withStatus(200)))
    wireMockServer.start()
    Await.result(WebhookLambda(mockS3Client).handleRequest(S3Event(List(s3EventNotificationRecord).asJava), mockContext), 1.second) shouldBe StatusCode(200)
    wireMockServer.stop()
  }
}
