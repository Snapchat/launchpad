#!/usr/bin/bash

set -eo pipefail

if [ -n "$GCP_STORAGE_BUCKET" ] && [ -n "$STORAGE_PATH" ]; then
  echo "Mounting GCS Fuse."
  mkdir -p "$STORAGE_PATH"
  gcsfuse "$GCP_STORAGE_BUCKET" "$STORAGE_PATH"
  echo "Mounting completed."
fi

# Start the application
java -jar launchpad.jar
