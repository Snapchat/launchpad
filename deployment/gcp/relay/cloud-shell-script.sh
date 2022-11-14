#!/bin/bash

set -e -u -o pipefail

PS3="Please select the number of your desired project: "
PROJECT_ARR=($(gcloud projects list --uri | sed 's/.*\/\(.*\)/\1/'))
if [ ${#PROJECT_ARR[@]} -gt 1 ]; then
  select ITEM in "${PROJECT_ARR[@]}"
  do
    PROJECT=$ITEM
    if [ -n "${PROJECT}" ]; then
      break
    fi
  done
else
  PROJECT="${PROJECT_ARR[0]}"
fi
gcloud config set project "${PROJECT}"

PS3="Please select the number of your desired region: "
REGION_ARR=(
  "us-central1"
  "us-east1"
  "us-west1"
  "europe-north1"
  "europe-west1"
  "europe-west4"
  "europe-north1"
  "asia-southeast1"
  "asia-northeast1"
  "asia-east1"
)
if [ ${#REGION_ARR[@]} -gt 1 ]; then
  select ITEM in "${REGION_ARR[@]}"
  do
    REGION=$ITEM
    if [ -n "${REGION}" ]; then
      break
    fi
  done
else
  REGION="${REGION_ARR[0]}"
fi

gcloud projects add-iam-policy-binding "${PROJECT}" \
  --member="user:$(gcloud auth list --format 'value(account)')" \
  --role="roles/storage.admin"

REMOTE_STATE_BUCKET="${PROJECT}-snap-launchpad-terraform"
gsutil ls -b "gs://${REMOTE_STATE_BUCKET}" || \
  gsutil mb "gs://${REMOTE_STATE_BUCKET}"
echo "prefix = \"terraform/state\"
bucket = \"${REMOTE_STATE_BUCKET}\"" > /terraform/backend.conf

LAUNCHPAD_VERSION=RELEASE_VERSION

if [ -z "$LAUNCHPAD_VERSION" ]; then
  LAUNCHPAD_VERSION="latest"
fi

export TF_VAR_PROJECT=${PROJECT}
export TF_VAR_REGION=${REGION}
export TF_VAR_LAUNCHPAD_VERSION=${LAUNCHPAD_VERSION}

terraform -chdir=/terraform init -backend-config=backend.conf
terraform -chdir=/terraform apply -auto-approve
