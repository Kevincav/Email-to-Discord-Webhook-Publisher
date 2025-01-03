import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.{RequestParametersEntity, ResponseElementsEntity, S3BucketEntity, S3Entity, S3EventNotificationRecord, S3ObjectEntity, UserIdentityEntity}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{CreateBucketRequest, PutObjectRequest}
import sttp.model.StatusCode

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*

class WebhookLambdaTest extends AnyFlatSpec with Matchers {
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
      "79104EXAMPLEB723"), S3Entity("testConfigRule", S3BucketEntity("testBucket", UserIdentityEntity("EXAMPLE"), "arn:aws:s3:::testBucket"),
      S3ObjectEntity("testKey", 5065717, "c2d226b2e97bec9265eb7e59d2dfac41", "2.0", ""), "s3SchemaVersion"), UserIdentityEntity("EXAMPLE"))

  "handle request" should "publish a webhook upon call" in {
    val localStack = LocalStackContainer(DockerImageName.parse("localstack/localstack:3")).withServices(LocalStackContainer.Service.S3)
    val s3Client: S3Client = S3Client.builder()
      .endpointOverride(localStack.getEndpoint)
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStack.getAccessKey, localStack.getSecretKey)))
      .region(Region.of(localStack.getRegion))
      .build()

    s3Client.createBucket(CreateBucketRequest.builder().bucket("testBucket").build())
    s3Client.putObject(PutObjectRequest.builder.bucket("bucketName").key("testKey").build, RequestBody.fromBytes(inputStreamText.getBytes))

    val wireMockServer: WireMockServer = WireMockServer(9999)
    wireMockServer.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse.withStatus(200)))
    wireMockServer.start()
    Await.result(WebhookLambda.handleEvent(S3Event(List(s3EventNotificationRecord).asJava)), 1.second).code shouldBe StatusCode(200)
    wireMockServer.stop()
  }
}
