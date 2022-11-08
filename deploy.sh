#!/bin/bash

set -e -o pipefail

INVALID_INPUT_ERROR=1
GIT_NOT_CLEAN_ERROR=2
NO_REGISTRY_AVAILABLE_ERROR=3

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

build_and_push_image() {
  cp "dockers/${PLATFORM}/Dockerfile" .
  cp "dockers/${PLATFORM}/start.sh" .

  if [ "${PLATFORM}" == "gcp" ]; then
    CONTAINER_REGISTRY="gcr.io/snap-launchpad-public"
  elif [ "${PLATFORM}" == "aws" ]; then
    CONTAINER_REGISTRY="public.ecr.aws/f0q4i5h2"
  else
    exit $NO_REGISTRY_AVAILABLE_ERROR
  fi

  if [ -z "$RELEASE_TAG" ]; then
    docker build --build-arg VERSION_TAG="$(git rev-parse --verify HEAD)" \
        -t "${CONTAINER_REGISTRY}/launchpad:$(git rev-parse --verify HEAD)" \
        -t "${CONTAINER_REGISTRY}/launchpad:latest" \
        .
  else
    docker build --build-arg VERSION_TAG="${RELEASE_TAG}" \
        -t "${CONTAINER_REGISTRY}/launchpad:${RELEASE_TAG}" \
        -t "${CONTAINER_REGISTRY}/launchpad:prod" \
        -t "${CONTAINER_REGISTRY}/launchpad:latest" \
        .
  fi

  docker push "${CONTAINER_REGISTRY}/launchpad" --all-tags

  echo "The image has been uploaded to $CONTAINER_REGISTRY/launchpad/$PLATFORM"
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
RELEASE_TAG=$2

# check_git_status

if [ "$PLATFORM" == "gcp" ] || [ "$PLATFORM" == "aws" ]
then
  build_and_push_image
else
  show_help
  exit $INVALID_INPUT_ERROR
fi
