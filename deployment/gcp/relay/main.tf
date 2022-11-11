variable "PROJECT" {
  type = string
}

variable "REGION" {
  type = string
}

variable "LAUNCHPAD_VERSION" {
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

resource "google_project_service" "run" {
  service = "run.googleapis.com"

  disable_dependent_services = true
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
  location = var.REGION

  template {
    spec {
      containers {
        image = "gcr.io/snap-launchpad-public/launchpad:${var.LAUNCHPAD_VERSION}"
        env {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "prod,conversion-relay"
        }
      }
      service_account_name = google_service_account.snap-launchpad.email
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }

  depends_on = [google_project_service.run]
}

output "cloud-run-url" {
  value = google_cloud_run_service.snap-launchpad.status.0.url
}
