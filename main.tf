// IAM Role

resource "aws_iam_role" "Discord-Email-Webhook-IAM" {
  name               = "${local.program_name}-iam-role"
  assume_role_policy = data.aws_iam_policy_document.Assume-Role-Lambda-Policy.json
}

// Lambda Function

resource "aws_lambda_function" "Discord-Email-Webhook" {
  runtime       = var.runtime
  role          = aws_iam_role.Discord-Email-Webhook-IAM.arn
  handler       = "WebhookLambda::handleRequest"
  function_name = "${local.program_name}-Discord-Email-Webhook"
  architectures = [var.architecture]
  s3_bucket     = var.lambda_bucket
  s3_key        = var.lambda_key
  environment {
    variables = {
      "WEBHOOK_ADDRESS" : var.webhook_address
    }
  }
  depends_on = [aws_iam_role.Discord-Email-Webhook-IAM]
}

// S3 Buckets

resource "aws_s3_bucket" "Discord-Email-Webhook-Bucket" {
  bucket = "${local.program_name}-emails"
}

resource "aws_s3_bucket_lifecycle_configuration" "Discord-Email-Webhook-Bucket-Lifecycle-Policy" {
  bucket = aws_s3_bucket.Discord-Email-Webhook-Bucket.bucket
  rule {
    id = "ExpireObjects"
    expiration {
      days = 7
    }
    status = "Enabled"
  }
  depends_on = [aws_s3_bucket.Discord-Email-Webhook-Bucket]
}

resource "aws_s3_bucket_notification" "Discord-Email-Webhook-Lambda-Trigger" {
  bucket = aws_s3_bucket.Discord-Email-Webhook-Bucket.bucket
  lambda_function {
    lambda_function_arn = aws_lambda_function.Discord-Email-Webhook.arn
    events = [
      "s3:ObjectCreated:Put",
      "s3:ObjectCreated:Post"
    ]
  }
  depends_on = [aws_s3_bucket.Discord-Email-Webhook-Bucket, aws_lambda_function.Discord-Email-Webhook]
}

// Setup Policies

resource "aws_iam_role_policy" "Discord-Email-Webhook-Bucket-Get-Object-Policy" {
  role       = aws_iam_role.Discord-Email-Webhook-IAM.name
  name       = "${local.program_name}-s3-get-object-policy"
  policy     = data.aws_iam_policy_document.S3-Get-Set-Object-Policy.json
  depends_on = [aws_iam_role.Discord-Email-Webhook-IAM, aws_s3_bucket.Discord-Email-Webhook-Bucket]
}

resource "aws_iam_role_policy" "Cloud-Log-Group-Policy" {
  role       = aws_iam_role.Discord-Email-Webhook-IAM.name
  name       = "${local.program_name}-log-group-policy"
  policy     = data.aws_iam_policy_document.Cloud-Log-Group-Policy.json
  depends_on = [aws_iam_role.Discord-Email-Webhook-IAM, aws_lambda_function.Discord-Email-Webhook]
}

// SES

resource "aws_ses_domain_identity" "Discord-Email-Webhook-Domain" {
  domain = var.domain_name
}

resource "aws_ses_active_receipt_rule_set" "Discord-Email-Webhook-Active-Ruleset" {
  rule_set_name = "${local.program_name}-rule-set"
  depends_on    = [aws_ses_domain_identity.Discord-Email-Webhook-Domain]
}

resource "aws_ses_receipt_rule" "Discord-Email-Webhook-Ruleset-Rule" {
  name          = "${local.program_name}-rule"
  rule_set_name = aws_ses_active_receipt_rule_set.Discord-Email-Webhook-Active-Ruleset.rule_set_name
  recipients    = [local.recipient_email]
  enabled       = true
  s3_action {
    bucket_name  = aws_s3_bucket.Discord-Email-Webhook-Bucket.bucket
    iam_role_arn = aws_iam_role.Discord-Email-Webhook-IAM.arn
    position     = 1
  }
  depends_on = [
    aws_ses_active_receipt_rule_set.Discord-Email-Webhook-Active-Ruleset,
    aws_s3_bucket.Discord-Email-Webhook-Bucket,
    aws_iam_role.Discord-Email-Webhook-IAM
  ]
}
