variable "PROJECT" {
  type = string
}

variable "REGION" {
  type = string
}

variable "DOMAIN" {
  type = string
}

locals {
  url = "tr-v2.${var.DOMAIN}"
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
  machine_type = "e2-standard-4"

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
  location = "us-central1"

  template {
    spec {
      containers {
        image = "gcr.io/snap-launchpad-public/launchpad/gcp:1.0.0"
        env {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "prod,mpc,batch-gcp"
        }
        env {
          name  = "GCP_STORAGE_BUCKET"
          value = google_storage_bucket.snap-launchpad.name
        }
        env {
          name  = "GCP_PROJECT_ID"
          value = var.PROJECT
        }
        env {
          name  = "STORAGE_PATH"
          value = "/mnt/gcs"
        }
        env {
          name  = "GCP_BATCH_INSTANCE_TEMPLATE"
          value = google_compute_instance_template.snap-launchpad-batch.name
        }
        env {
          name  = "PUBLIC_URL"
          value = local.url
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
  name     = local.url

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
