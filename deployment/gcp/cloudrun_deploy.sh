#!/bin/bash

set -e -u -o pipefail

BLUE="\e[94m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

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


PS3="Please select the number of your desired region: "
REGION_ARR=(
  "asia-east1"
  "asia-east2"
  "asia-northeast1"
  "asia-northeast2"
  "asia-northeast3"
  "asia-south1"
  "asia-south2"
  "asia-southeast1"
  "asia-southeast2"
  "australia-southeast1"
  "australia-southeast2"
  "europe-central2"
  "europe-north1"
  "europe-southwest1"
  "europe-west1"
  "europe-west2"
  "europe-west3"
  "europe-west4"
  "europe-west6"
  "europe-west8"
  "europe-west9"
  "northamerica-northeast1"
  "northamerica-northeast2"
  "southamerica-east1"
  "southamerica-west1"
  "us-central1"
  "us-central2"
  "us-east1"
  "us-east4"
  "us-east5"
  "us-south1"
  "us-west1"
  "us-west2"
  "us-west3"
  "us-west4"
)
if [ ${#REGION_ARR[@]} -gt 1 ]; then
  select ITEM in "${REGION_ARR[@]}"
  do
    REGION=$ITEM
    if [ -n "${PROJECT}" ]; then
      break
    fi
  done
else
  REGION="${REGION_ARR[0]}"
fi


PS3="Please select the number of your desired launchpad mode: "
MODE_ARR=(mpc relay)
if [ ${#MODE_ARR[@]} -gt 1 ]; then
  select ITEM in "${MODE_ARR[@]}"
  do
    MODE=$ITEM
    if [ -n "${MODE}" ]; then
      break
    fi
  done
else
  MODE="${MODE_ARR[0]}"
fi

DOMAIN_URL=""
while [ -z "$DOMAIN_URL" ]
do
  echo -e "Please enter your domain url: "
  read -r DOMAIN_URL
done


echo -e "${BLUE}1. Set project to ${PROJECT}: ${ENDCOLOR}"
echo "gcloud config set project ${PROJECT}"
gcloud config set project "${PROJECT}"


echo -e "${BLUE}2. Enable Cloud IAM Service:${ENDCOLOR}"
echo "gcloud services enable iam.googleapis.com"
gcloud services enable iam.googleapis.com


echo -e "${BLUE}3. Enable batch api:${ENDCOLOR}"
echo "gcloud services enable batch.googleapis.com"
gcloud services enable batch.googleapis.com


echo -e "${BLUE}4. Enable Cloud Run Service:${ENDCOLOR}"
echo "gcloud services enable run.googleapis.com"
gcloud services enable run.googleapis.com


echo -e "${BLUE}5. Check if launchpad service account exists:${ENDCOLOR}"
role=$(gcloud iam service-accounts get-iam-policy "snap-launchpad-service-account@${PROJECT}.iam.gserviceaccount.com" > /dev/null 2>&1) || role=""
if [ -z "$role" ]
then
    echo -e "${BLUE}Create service account:${ENDCOLOR}"
    echo "gcloud iam service-accounts create snap-launchpad-service-account --display-name=\"Launchpad Service Account\""
    gcloud iam service-accounts create snap-launchpad-service-account --display-name="Launchpad Service Account"

    echo -e "${BLUE}Giving owner permission to the service account:${ENDCOLOR}"
    echo "gcloud iam service-accounts add-iam-policy-binding snap-launchpad-service-account@$PROJECT.iam.gserviceaccount.com --member=\"serviceAccount:snap-launchpad-service-account@$PROJECT.iam.gserviceaccount.com\" --role='roles/owner'"
    gcloud iam service-accounts add-iam-policy-binding "snap-launchpad-service-account@${PROJECT}.iam.gserviceaccount.com" --member="serviceAccount:snap-launchpad-service-account@$PROJECT.iam.gserviceaccount.com" --role='roles/owner'
else
    echo -e "${GREEN}Service account snap-launchpad-service-account@$PROJECT.iam.gserviceaccount.com already exists${ENDCOLOR}"
fi


echo -e "${BLUE}6. Setup GCS bucket:${ENDCOLOR}"
BUCKET="${PROJECT}-snap-launchpad"
echo "gsutil ls -b \"gs://${BUCKET}\" || gsutil mb -l \"${REGION}\" \"gs://${BUCKET}\""
gsutil ls -b "gs://${BUCKET}" > /dev/null 2>&1 || gsutil mb -l "${REGION}" "gs://${BUCKET}"


echo -e "${BLUE}7. Deploy cloud run:${ENDCOLOR}"
gcloud beta run deploy snap-launchpad \
  --project="${PROJECT}" \
  --platform=managed \
  --service-account="snap-launchpad-service-account@${PROJECT}.iam.gserviceaccount.com" \
  --allow-unauthenticated \
  --ingress=internal-and-cloud-load-balancing \
  --region="${REGION}" \
  --image="gcr.io/snap-launchpad/launchpad" \
  --concurrency 1000 \
  --timeout 15 \
  --cpu 1 \
  --memory 1Gi \
  --min-instances 1 \
  --min-instances 4 \
  --no-cpu-throttling \
  --execution-environment gen2 \
  --set-env-vars "^:^SPRING_PROFILES_ACTIVE=${MODE},batch-gcp:GCP_PROJECT_ID=${PROJECT}:GCP_REGION=${REGION}:GCP_STORAGE_BUCKET=${BUCKET}:STORAGE_PATH=/mnt/gcs"


echo -e "${BLUE}8. Setup load balancer${ENDCOLOR}"
gcloud compute network-endpoint-groups describe snap-launchpad --region "${REGION}" > /dev/null 2>&1 ||
  gcloud compute network-endpoint-groups create snap-launchpad \
    --region="${REGION}" \
    --network-endpoint-type=serverless \
    --cloud-run-service=snap-launchpad
gcloud compute backend-services describe snap-launchpad --global > /dev/null 2>&1 ||
  gcloud compute backend-services create snap-launchpad \
    --load-balancing-scheme=EXTERNAL_MANAGED \
    --global
gcloud compute backend-services list --filter="name=('snap-launchpad')" --uri | grep -q "snap-launchpad" ||
  gcloud compute backend-services add-backend snap-launchpad \
    --global \
    --network-endpoint-group=snap-launchpad \
    --network-endpoint-group-region="${REGION}"
gcloud compute url-maps describe snap-launchpad > /dev/null 2>&1 ||
  gcloud compute url-maps create snap-launchpad \
    --default-service snap-launchpad
gcloud compute ssl-certificates describe snap-launchpad > /dev/null 2>&1 ||
  gcloud compute ssl-certificates create snap-launchpad --domains="${DOMAIN_URL}" --global
gcloud compute target-https-proxies describe snap-launchpad > /dev/null 2>&1 ||
  gcloud compute target-https-proxies create snap-launchpad \
    --ssl-certificates=snap-launchpad \
    --url-map=snap-launchpad
gcloud compute addresses describe snap-launchpad --global > /dev/null 2>&1 || \
  gcloud compute addresses create snap-launchpad --network-tier=PREMIUM --ip-version=IPV4 --global
gcloud compute forwarding-rules create snap-launchpad \
  --load-balancing-scheme=EXTERNAL_MANAGED \
  --network-tier=PREMIUM \
  --address=snap-launchpad \
  --target-https-proxy=snap-launchpad \
  --global \
  --ports=443

IP=$(gcloud --format="value(address)" compute addresses describe snap-launchpad --global)
echo -e "${GREEN}=======================================================================${ENDCOLOR}"
echo -e "${GREEN}Please go to your domain registrar and add an A record for your domain.${ENDCOLOR}"
echo -e "${GREEN}Domain: ${DOMAIN_URL}${ENDCOLOR}"
echo -e "${GREEN}IP: ${IP}"
echo -e "${GREEN}=======================================================================${ENDCOLOR}"
