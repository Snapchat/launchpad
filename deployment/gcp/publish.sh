#!/bin/bash

set -e

docker build . -t launchpad-cloud-shell
docker tag launchpad-cloud-shell gcr.io/snap-launchpad-public/launchpad-cloud-shell
docker push gcr.io/snap-launchpad-public/launchpad-cloud-shell