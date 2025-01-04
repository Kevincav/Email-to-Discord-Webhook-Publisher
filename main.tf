// IAM Role

resource "aws_iam_role" "discord-email-webhook" {
  name               = "${local.program_name}-iam-role"
  assume_role_policy = data.aws_iam_policy_document.policy_document.json
}

// Lambda Function

resource "aws_lambda_function" "discord-email-webhook" {
  runtime       = var.runtime
  role          = aws_iam_role.discord-email-webhook.arn
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
}

// S3 Buckets

resource "aws_s3_bucket" "discord-email-webhook" {
  bucket = "${local.program_name}-emails"
}

resource "aws_s3_bucket_lifecycle_configuration" "discord-email-webhook" {
  bucket = aws_s3_bucket.discord-email-webhook.bucket
  rule {
    id = "ExpireObjects"
    expiration {
      days = 7
    }
    status = "Enabled"
  }
}

resource "aws_s3_bucket_notification" "discord-email-webhook" {
  bucket = aws_s3_bucket.discord-email-webhook.id
  lambda_function {
    lambda_function_arn = aws_lambda_function.discord-email-webhook.arn
    events = [
      "s3:ObjectCreated:Put",
      "s3:ObjectCreated:Post"
    ]
  }
}

// Setup Policies

resource "aws_iam_role_policy" "discord-email-webhook" {
  role   = aws_iam_role.discord-email-webhook.name
  name   = "${local.program_name}-s3-get-object-policy"
  policy = data.aws_iam_policy_document.s3-get-object-policy.json
}

resource "aws_iam_role_policy" "cloud_log_group" {
  role   = aws_iam_role.discord-email-webhook.name
  name   = "${local.program_name}-log-group-policy"
  policy = data.aws_iam_policy_document.cloud_log_group.json
}

resource "aws_s3_bucket_policy" "discord-email-webhook" {
  bucket = aws_s3_bucket.discord-email-webhook.bucket
  policy = data.aws_iam_policy_document.s3_bucket_policy.json
}

// SES

resource "aws_ses_domain_identity" "discord-email-webhook" {
  domain = var.domain_name
}

resource "aws_ses_receipt_rule_set" "discord-email-webhook" {
  rule_set_name = "${local.program_name}-rule-set"
}

resource "aws_ses_receipt_rule" "discord-email-webhook" {
  name          = "${local.program_name}-rule"
  rule_set_name = aws_ses_receipt_rule_set.discord-email-webhook.rule_set_name
  recipients    = [local.recipient_email]
  enabled       = true
  s3_action {
    bucket_name = aws_s3_bucket.discord-email-webhook.id
    position    = 1
  }
}
