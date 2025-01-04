data "aws_caller_identity" "AWS-Details" {}

data "aws_iam_policy_document" "Assume-Role-Lambda-Policy" {
  version = "2012-10-17"
  statement {
    sid    = "LambdaAssumeRole"
    effect = "Allow"
    principals {
      identifiers = ["lambda.amazonaws.com", "ses.amazonaws.com"]
      type        = "Service"
    }
    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "S3-Get-Set-Object-Policy" {
  version = "2012-10-17"
  statement {
    sid       = "S3Permissions"
    effect    = "Allow"
    actions   = ["s3:GetObject", "s3:PutObject", "s3:PostObject"]
    resources = ["${aws_s3_bucket.Discord-Email-Webhook-Bucket.arn}/*"]
  }
  statement {
    sid       = "SESPutObject"
    effect    = "Allow"
    actions   = ["s3:PutObject", "s3:PostObject"]
    resources = [aws_s3_bucket.Discord-Email-Webhook-Bucket.arn]
  }
}

data "aws_iam_policy_document" "Cloud-Log-Group-Policy" {
  version = "2012-10-17"
  statement {
    sid       = "CreateLogGroup"
    effect    = "Allow"
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${var.region}:${data.aws_caller_identity.AWS-Details.account_id}:*"]
  }
  statement {
    sid     = "CreateLogGroupStream"
    effect  = "Allow"
    actions = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = [
      "arn:aws:logs:${var.region}:${data.aws_caller_identity.AWS-Details.account_id}:log-group:/aws/lambda/${aws_lambda_function.Discord-Email-Webhook.function_name}:*"
    ]
  }
}
