variable "PROJECT" {
  type = string
}

variable "REGION" {
  type = string
}

variable "DOMAIN" {
  type = string
}

terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "4.33.0"
    }
  }
  backend "gcs" {}
}

provider "google" {
  project = var.PROJECT
  region  = var.REGION
}

resource "google_service_account" "snap-launchpad" {
  account_id   = "snap-launchpad"
  display_name = "snap-launchpad"
  project      = var.PROJECT
}

data "google_iam_policy" "cloud-run-invoker-public" {
  binding {
    role = "roles/run.invoker"
    members = [
      "allUsers",
    ]
  }
}

resource "google_cloud_run_service_iam_policy" "noauth" {
  location    = google_cloud_run_service.snap-launchpad.location
  service     = google_cloud_run_service.snap-launchpad.name
  policy_data = data.google_iam_policy.cloud-run-invoker-public.policy_data
}

resource "google_cloud_run_service" "snap-launchpad" {
  name     = "snap-launchpad"
  location = "us-central1"

  template {
    spec {
      containers {
        image = "gcr.io/snap-launchpad-public/launchpad/gcp:prod"
        env {
          name  = "SPRING_PROFILES_ACTIVE",
          value = "prod,relay",
        }
      }
      service_account_name = google_service_account.snap-launchpad.email
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }
}

resource "google_cloud_run_domain_mapping" "snap-launchpad" {
  location = "us-central1"
  name     = "tr-v2.${var.DOMAIN}"

  metadata {
    namespace = var.PROJECT
  }

  spec {
    route_name = google_cloud_run_service.snap-launchpad.name
  }
}

output "dns" {
  value = element(element(google_cloud_run_domain_mapping.snap-launchpad.status, 0).resource_records, 0)
}
