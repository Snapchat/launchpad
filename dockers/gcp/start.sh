#!/usr/bin/bash

set -u -eo pipefail

echo "Mounting GCS Fuse."
mkdir -p "$STORAGE_PATH"
gcsfuse "$GCP_STORAGE_BUCKET" "$STORAGE_PATH"
echo "Mounting completed."

# Start the application
java -jar launchpad.jar
