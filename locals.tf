locals {
  account_id   = data.aws_caller_identity.discord-email-webhook.account_id
  program_name = "${var.recipient}-${var.discord_name}"
  recipient    = "${var.recipient}@${var.discord_name}"
}