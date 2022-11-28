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

resource "google_project_service" "run" {
  service = "run.googleapis.com"

  disable_dependent_services = true
}

resource "google_project_service" "batch" {
  service = "batch.googleapis.com"

  disable_dependent_services = true
}

resource "google_project_service" "iamcredentials" {
  service = "iamcredentials.googleapis.com"

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

resource "google_project_iam_binding" "storage-admin-snap-launchpad-service-account" {
  project = var.PROJECT
  role    = "roles/storage.admin"
  members = [
    "serviceAccount:${google_service_account.snap-launchpad.email}",
  ]
}

resource "google_project_iam_binding" "iam-token-snap-launchpad-service-account" {
  project = var.PROJECT
  role    = "roles/iam.serviceAccountTokenCreator"
  members = [
    "serviceAccount:${google_service_account.snap-launchpad.email}",
  ]
}

resource "google_storage_bucket" "snap-launchpad" {
  name          = "${var.PROJECT}-snap-launchpad"
  location      = var.REGION
  force_destroy = true
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
        # Enable HTTP/2
        # https://cloud.google.com/run/docs/configuring/http2
        ports {
          name           = "h2c"
          container_port = 8080
        }
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
          name  = "IDENTITY_PROVIDER_URL"
          value = "https://gcp.api.snapchat.com/pet/v1/authorization"
        }
        env {
          name  = "STORAGE_PREFIX"
          value = "gs://${google_storage_bucket.snap-launchpad.name}"
        }
        env {
          name  = "MPC_GCP_BATCH_INSTANCE_TEMPLATE"
          value = google_compute_instance_template.snap-launchpad-batch.name
        }
        env {
          name  = "MPC_JOB_CONFIG_PUBLISHER_URL"
          value = "https://gcp.api.snapchat.com/pet/v1/mpc/job-configs"
        }
        env {
          name  = "MPC_JOB_IMAGE"
          value = "gcr.io/snap-launchpad-public/snap-mpc/onedocker:20b3bec3e2d75e5dfd380b0a7929f6549a2bf964"
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
