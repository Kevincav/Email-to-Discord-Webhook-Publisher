data "aws_caller_identity" "discord-email-webhook" {}

data "aws_iam_policy_document" "policy_document" {
  version = "2012-10-17"
  statement {
    effect = "Allow"
    principals {
      identifiers = ["lambda.amazonaws.com"]
      type        = "Service"
    }
    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "s3-get-object-policy" {
  version = "2012-10-17"
  statement {
    sid       = "S3GetObject"
    effect    = "Allow"
    actions   = ["s3:GetObject"]
    resources = [aws_s3_bucket.discord-email-webhook.arn]
  }
}

data "aws_iam_policy_document" "cloud_log_group" {
  version = "2012-10-17"
  statement {
    effect    = "Allow"
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${var.region}:${data.aws_caller_identity.discord-email-webhook.account_id}:*"]
  }
  statement {
    effect    = "Allow"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:${var.region}:${data.aws_caller_identity.discord-email-webhook.account_id}:log-group:/aws/lambda/${aws_lambda_function.discord-email-webhook.function_name}:*"]
  }
}

data "aws_iam_policy_document" "s3_bucket_policy" {
  version = "2012-10-17"
  statement {
    sid    = "AllowLambdaGets"
    effect = "Allow"
    principals {
      identifiers = [aws_iam_role.discord-email-webhook.arn]
      type        = "AWS"
    }
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.discord-email-webhook.bucket}/*"]
  }
  statement {
    sid    = "AllowSESPuts"
    effect = "Allow"
    principals {
      identifiers = ["ses.amazonaws.com"]
      type        = "Service"
    }
    actions   = ["s3:PutObject"]
    resources = ["${aws_s3_bucket.discord-email-webhook.bucket}/*"]
    condition {
      test     = "StringEquals"
      values   = [data.aws_caller_identity.discord-email-webhook.account_id]
      variable = "aws:Referer"
    }
  }
}
