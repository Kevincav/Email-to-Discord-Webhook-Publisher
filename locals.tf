locals {
  account_id   = data.aws_caller_identity.discord-email-webhook.account_id
  lambda_path  = "${path.root}/out/artifacts/DiscordEmailWebhook_jar/DiscordEmailWebhook.jar"
  program_name = "${var.recipient}-${var.discord_name}"
  recipient    = "${var.recipient}@${var.discord_name}"
}