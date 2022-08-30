#!/bin/bash

set -e -u -o pipefail

INVALID_INPUT_ERROR=1
GIT_NOT_CLEAN_ERROR=2

CONTAINER_REGISTRY="gcr.io/snap-launchpad-public"

function cleanup()
{
    rm -rf Dockerfile
    rm -rf start.sh
}
trap cleanup EXIT

show_help() {
cat << EOF
Launchpad Docker Build Script
Usage: ./deploy.sh <gcp|aws>
Params:
  -h  show this help text
Example:
  $ ./deploy.sh gcp
EOF
}

check_git_status() {
  if [ -n "$(git status --porcelain)" ]; then
    # Working directory clean
    echo "Git repo not clean... Please commit your changes before deployment..."
    exit $GIT_NOT_CLEAN_ERROR
  fi
}

build_gcp() {
  cp dockers/gcp/Dockerfile .
  cp dockers/gcp/start.sh .

  docker build \
    -t "${CONTAINER_REGISTRY}/launchpad/gcp:$(git rev-parse --verify HEAD)" \
    -t "${CONTAINER_REGISTRY}/launchpad/gcp:latest" \
    .
  docker push "${CONTAINER_REGISTRY}/launchpad/gcp" --all-tags

  echo "The image has been uploaded to $CONTAINER_REGISTRY/launchpad/gcp"
}

build_aws() {
  cp dockers/aws/Dockerfile .
  cp dockers/aws/start.sh .

  docker build \
    -t "${CONTAINER_REGISTRY}/launchpad/aws:$(git rev-parse --verify HEAD)" \
    -t "${CONTAINER_REGISTRY}/launchpad/aws:latest" \
    .
  docker push "${CONTAINER_REGISTRY}/launchpad/aws" --all-tags

  echo "The image has been uploaded to $CONTAINER_REGISTRY/launchpad/aws"
}

while getopts ":h" opt; do
  case $opt in
    h)
      show_help
      exit 0
      ;;
    \?)
      show_help >&2
      echo "Invalid argument: $OPTARG" &2
      exit $INVALID_INPUT_ERROR
      ;;
  esac
done

shift $((OPTIND-1))

PLATFORM=$1

# check_git_status

if [ "$PLATFORM" == "gcp" ]; then
  build_gcp
elif [ "$PLATFORM" == "aws" ]; then
  build_aws
else
  show_help
  exit $INVALID_INPUT_ERROR
fi
