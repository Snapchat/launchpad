# Steps to publish new image:
## MPC
```bash
docker build -t launchpad-cloud-shell --build-arg LAUNCHPAD_MODE=mpc .
docker tag launchpad-cloud-shell gcr.io/snap-launchpad-public/launchpad-cloud-shell/mpc
docker push gcr.io/snap-launchpad-public/launchpad-cloud-shell/mpc
```
## Relay
```bash
docker build -t launchpad-cloud-shell --build-arg LAUNCHPAD_MODE=relay .
docker tag launchpad-cloud-shell gcr.io/snap-launchpad-public/launchpad-cloud-shell/relay
docker push gcr.io/snap-launchpad-public/launchpad-cloud-shell/relay
```
