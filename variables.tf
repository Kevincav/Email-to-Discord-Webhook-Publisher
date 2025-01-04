variable "aws_access_key_id" {
  description = "AWS Access Key ID"
  type        = string
}

variable "aws_secret_access_key" {
  description = "AWS Secret Key"
  type        = string
}

variable "discord_name" {
  description = "Discord Name"
  type        = string
}

variable "domain_name" {
  description = "Domain Name"
  type        = string
}

variable "lambda_bucket" {
  description = "S3 Bucket of the Lambda Function"
  type        = string
}

variable "lambda_path" {
  description = "Path of the Lambda Function"
  type        = string
}

variable "recipient" {
  description = "Recipient"
  type        = string
}

variable "region" {
  description = "AWS Region"
  type        = string
}

variable "webhook_address" {
  description = "Discord Webhook Address"
  type        = string
}

variable "architecture" {
  description = "Runtime Architecture"
  type        = string
  default     = "arm64"
}

variable "runtime" {
  description = "Runtime Environment"
  type        = string
  default     = "java21"
}
