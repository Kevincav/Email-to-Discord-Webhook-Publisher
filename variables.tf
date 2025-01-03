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

variable "s3_bucket" {
  description = "S3 path of the Lambda Function"
  type        = string
}

variable "recipient" {
  description = "Recipient"
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

variable "region" {
  description = "AWS Region"
  type        = string
  default     = "us-east-1"
}

variable "runtime" {
  description = "Runtime Environment"
  type        = string
  default     = "java21"
}
