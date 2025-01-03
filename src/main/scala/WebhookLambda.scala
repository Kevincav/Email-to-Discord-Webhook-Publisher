import com.amazonaws.services.lambda.runtime.events.S3Event
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.S3Client
import sttp.client4.Response
import webhook.{Webhook, WebhookData}

import java.io.InputStream
import scala.concurrent.Future

object WebhookLambda {
  private def getEmailFromS3(bucket: String, key: String): InputStream =
    S3Client.create().getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())

  def handleEvent(s3event: S3Event): Future[Response[String]] = {
    val (bucket, key) = (s3event.getRecords.get(0).getS3.getBucket.getName, s3event.getRecords.get(0).getS3.getObject.getKey)
    Webhook.publish(sys.env.getOrElse("WEBHOOK_ADDRESS", "http://localhost:9999/webhook"),
      WebhookData(List(Webhook.parseEmail(getEmailFromS3(bucket, key)))))
  }
}
