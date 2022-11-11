variable "PROJECT" {
  type = string
}

variable "REGION" {
  type = string
}

variable "LAUNCHPAD_VERSION" {
  type = string
}

variable "ORGANIZATION_ID" {
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

resource "google_project_service" "batch" {
  service = "batch.googleapis.com"

  disable_dependent_services = true
}

resource "google_project_service" "run" {
  service = "run.googleapis.com"

  disable_dependent_services = true
}

resource "google_service_account" "snap-launchpad" {
  account_id   = "snap-launchpad"
  display_name = "snap-launchpad"
  project      = var.PROJECT
}

resource "google_project_iam_binding" "batch-admin-snap-launchpad-service-account" {
  project = var.PROJECT
  role    = "roles/batch.jobsAdmin"
  members = [
    "serviceAccount:${google_service_account.snap-launchpad.email}",
  ]
}

resource "google_storage_bucket" "snap-launchpad" {
  name          = "${var.PROJECT}-snap-launchpad"
  location      = var.REGION
  force_destroy = true
}

resource "google_storage_bucket_iam_member" "snap-launchpad" {
  bucket = google_storage_bucket.snap-launchpad.name
  role = "roles/storage.admin"
  member = "serviceAccount:${google_service_account.snap-launchpad.email}"
}

resource "google_compute_network" "snap-launchpad" {
  name = "snap-launchpad"
  auto_create_subnetworks = true
}

resource "google_compute_instance_template" "snap-launchpad-batch" {
  machine_type = "e2-standard-32"

  // boot disk
  disk {}

  network_interface {
    network = google_compute_network.snap-launchpad.name
  }

  scheduling {
    provisioning_model = "STANDARD"
  }

  service_account {
    email  = google_service_account.snap-launchpad.email
    scopes = ["cloud-platform"]
  }
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
          value = "prod,conversion-log,mpc-gcp"
        }
        env {
          name  = "ORGANIZATION_ID"
          value = var.ORGANIZATION_ID
        }
        env {
          name  = "CONVERSION_LOG_STORAGE_PREFIX"
          value = "gs://${google_storage_bucket.snap-launchpad.name}"
        }
        env {
          name  = "MPC_GCP_BATCH_INSTANCE_TEMPLATE"
          value = google_compute_instance_template.snap-launchpad-batch.name
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
