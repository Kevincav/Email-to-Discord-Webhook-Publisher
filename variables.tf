variable "discord_name" {
  description = "Discord Name"
  type        = string
}

variable "domain_name" {
  description = "Domain Name"
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
