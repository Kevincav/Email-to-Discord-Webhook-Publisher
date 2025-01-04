locals {
  account_id      = data.aws_caller_identity.AWS-Details.account_id
  program_name    = "${var.recipient}-${var.discord_name}"
  recipient_email = "${var.recipient}@${var.discord_name}"
}