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

DOMAIN_VERIFICATION_URL="https://search.google.com/search-console/welcome"
VERIFICATION_PROMPT_0="Before you begin, "
VERIFICATION_PROMPT_1="please go to ${DOMAIN_VERIFICATION_URL} to verify your domain with Google first."
VERIFICATION_PROMPT_2="After verifying your domain, press enter to continue..."
echo "${VERIFICATION_PROMPT_0}${VERIFICATION_PROMPT_1}"
echo "${VERIFICATION_PROMPT_2}"
while : ; do
    read -r
    DOMAIN_ARR=($(gcloud domains list-user-verified --format 'value(id)'))
    if [ ${#DOMAIN_ARR[@]} -gt 0 ]; then
      break
    fi
    echo "Looks like the domain verification has not been picked up by Google yet. Please wait a few minutes..."
    echo "Press enter to continue..."
done

PS3="Please select the number of your desired domain: "
if [ ${#DOMAIN_ARR[@]} -gt 1 ]; then
  select ITEM in "${DOMAIN_ARR[@]}"
  do
    DOMAIN=$ITEM
    if [ -n "${DOMAIN}" ]; then
      break
    fi
  done
else
  DOMAIN="${DOMAIN_ARR[0]}"
fi

gcloud projects add-iam-policy-binding "${PROJECT}" \
  --member="user:$(gcloud auth list --format 'value(account)')" \
  --role="roles/storage.admin"

REMOTE_STATE_BUCKET="${PROJECT}-snap-launchpad-terraform"
gsutil ls -b "gs://${REMOTE_STATE_BUCKET}" || \
  gsutil mb "gs://${REMOTE_STATE_BUCKET}"
echo "prefix = \"terraform/state\"
bucket = \"${REMOTE_STATE_BUCKET}\"" > /terraform/backend.conf

export TF_VAR_PROJECT=${PROJECT}
export TF_VAR_REGION=${REGION}
export TF_VAR_DOMAIN=${DOMAIN}

terraform -chdir=/terraform init -backend-config=backend.conf
terraform -chdir=/terraform apply -auto-approve
echo "Please add the DNS record above to your registrar"
