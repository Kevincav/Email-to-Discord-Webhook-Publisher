import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.S3Event
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.S3Client
import sttp.client4.Response
import webhook.{Webhook, WebhookData}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class WebhookLambda(s3Client: S3Client) extends RequestHandler[S3Event, Response[String]] {
  def this() = this(S3Client.create())

  private def getEmailFromS3(bucket: String, key: String) =
    s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())

  override def handleRequest(s3event: S3Event, context: Context): Response[String] = {
    val (bucket, key) = (s3event.getRecords.get(0).getS3.getBucket.getName, s3event.getRecords.get(0).getS3.getObject.getKey)
    val result = Webhook.publish(sys.env.getOrElse("WEBHOOK_ADDRESS", "http://localhost:10000/webhook"),
      WebhookData(List(Webhook.parseEmail(getEmailFromS3(bucket, key)))))
    Await.result(result, 10.seconds)
  }
}
