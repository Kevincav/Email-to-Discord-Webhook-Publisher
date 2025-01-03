import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import sttp.client4.Response
import webhook.{Webhook, WebhookData}

import java.io.InputStream
import scala.concurrent.Future

case class InputData(s3Client: AmazonS3 = AmazonS3ClientBuilder.defaultClient(),
                     webhookAddress: String = sys.env.getOrElse("WEBHOOK_ADDRESS", "http://localhost:9999/webhook"))

case class WebhookLambda(inputData: InputData = InputData()) {
  private def getEmailFromS3(bucket: String, key: String): InputStream = inputData.s3Client.getObject(bucket, key).getObjectContent

  def handleEvent(s3event: S3Event): Future[Response[String]] = {
    val (bucket, key) = (s3event.getRecords.get(0).getS3.getBucket.getName, s3event.getRecords.get(0).getS3.getObject.getKey)
    Webhook.publish(inputData.webhookAddress, WebhookData(List(Webhook.parseEmail(getEmailFromS3(bucket, key)))))
  }
}
