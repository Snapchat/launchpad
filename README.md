# SC Gateway

[![Build Launchpad](https://github.com/Snapchat/launchpad/actions/workflows/ci-build.yml/badge.svg)](https://github.com/Snapchat/launchpad/actions/workflows/ci-build.yml)

## Deploy it on Google Cloud Run
### If you want to run with MPC (Recommended):
[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://shell.cloud.google.com/?cloudshell_image=gcr.io/snap-launchpad-public/launchpad-cloud-shell/mpc:latest&show=terminal)
### If you want to run with Relay mode only:
[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://shell.cloud.google.com/?cloudshell_image=gcr.io/snap-launchpad-public/launchpad-cloud-shell/relay:latest&show=terminal)

## Deploy it on AWS Cloudformation
### If you want to run with MPC (Recommended):
[![](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?#/stacks/create/template?stackName=snap-launchpad&templateURL=https://snap-launchpad-public.s3.amazonaws.com/cloudformation/templcate-mpc.yaml)
### If you want to run with Relay mode only:
[![](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?#/stacks/create/template?stackName=snap-launchpad&templateURL=https://snap-launchpad-public.s3.amazonaws.com/cloudformation/templcate-relay.yaml)

## Updated front-end code:

```
<!-- Snap Pixel Code -->
<script type='text/javascript'>
  (function(e,t,n){if(e.snaptr)return;var a=e.snaptr=function()
          {a.handleRequest?a.handleRequest.apply(a,arguments):a.queue.push(arguments)};
          a.queue=[];var s='script';r=t.createElement(s);r.async=!0;
          r.src=n;var u=t.getElementsByTagName(s)[0];
          u.parentNode.insertBefore(r,u);})(window,document,
  '{{***HOST_URL_GOES_HERE***}}/scevent.min.js');

        snaptr('init', '{{***PIXEL_ID_GOES_HERE***}}', {
  'user_email': '__INSERT_USER_EMAIL__'
              });

        snaptr('track', 'PAGE_VIEW');

</script>
<!-- End Snap Pixel Code -->
```

## Run Locally:
1. Start the local server: `./gradlew bootRun`
1. Open: [localhost:8080](http://localhost:8080)

## To Build a dockerimage:

*Prerequisites:*

* Install Docker (if you don't already have it)

### Creating the Image

We utilize Google's Cloud Function Builder [buildpack](https://github.com/GoogleCloudPlatform/buildpacks) to ensure consistency with what will be generated on Google Cloud Functions

```
./gradlew bootBuildImage --builder=gcr.io/buildpacks/builder:v1 --imageName=snap-pixel-gateway
```

After running that command you should have a docker image named `snap-pixel-gateway`

This image can then be pushed to a repository with `docker push` or exported as a `.tar.gz` with `docker export`

### Running the container 

To run the built contianer, you may simply:

```
docker run --rm -p 8080:8080 snap-pixel-gateway
```

And the server should be up and running on port `8080`

While this is suitable for very testing and development, in production you should use something like `docker compose` or `kubernetes` or utilize your hosting architecture's container orchestration such as Amazon ECS to automatically scale up and down and ensure health of containers.

### Local development

Run unit tests with
```
./gradlew test
```

Run and apply the linter
```
./gradlew spotlessApply
```

Custom configurations can be found under
`src/main/resources/config.json`
