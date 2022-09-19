# Conversions API Launchpad

[![Build Launchpad](https://github.com/Snapchat/launchpad/actions/workflows/ci-build.yml/badge.svg)](https://github.com/Snapchat/launchpad/actions/workflows/ci-build.yml)

## Project Description

Conversions API Launchpad is a new way for Advertisers to share conversion data with Snap. This no-code solution enables a simple configuration of a new server to automatically share conversion events to Snap’s Conversions API.

Conversions API Launchpad is an open source, production-ready docker image intended to be hosted on Advertiser infrastructure. Advertisers can view the code or pull the docker image for Conversions API Launchpad from the link to its [Github page](https://github.com/Snapchat/).

The Conversions API can provide immediate performance benefits such as reduced CPA, improved targeting and optimization on ad campaigns, along with improved measurement capabilities.  [Learn more](https://businesshelp.snapchat.com/s/article/conversions-api?language=en_US).

In addition to the overall benefits of the Conversions API, Conversions API Launchpad will offer additional benefits such as:

-   **Speed:** most advertisers can complete the integration in as little as a few hours
-   **Reduced Technical Lift:** most will be able to complete this integration with limited IT support.
-   **Enhanced Privacy Control:** if required the Launchpad can be configured to use a Privacy Enhancing Technology (PET) such no individual user data leaves your servers. To learn more about this mode visit the [MPC](#Multi-Party-Compute-MPC) section

## Installation

Currently the easiest way to deploy Launchpad is via our *1-click launch buttons* on either *Google Cloud Run* or *AWS CloudFormation*:

Currently the supported methods of deployment are on Google Cloud or AWS. _Click here for [MPC](#Multi-Party-Compute-MPC)_

### Launchpad Server Installation
#### Google Cloud Run


[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://shell.cloud.google.com/?cloudshell_image=gcr.io/snap-launchpad-public/launchpad-cloud-shell/relay:prod&show=terminal)

After clicking it, you will be asked to select a Google Cloud Project and Region to deploy your instance to, you may create/select any project and should choose a region that is close to most of your customers. 

#### AWS CloudFormation

[![](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?#/stacks/create/template?stackName=snap-launchpad&templateURL=https://snap-launchpad-public.s3.amazonaws.com/cloudformation/template-relay.yml)

We also have provided a CloudFormation Stack button configured to deploy a Launchpad instance on AWS CloudFormation.

#### Other 

If you wish to run a Launchpad in some way other than the methods above, then all that is required is to [Build the Docker Image]() and run it in such a way that it can receive traffic.

As your instance receives valid pixel or Conversions API requests it should relay them to our servers without any additional configuration. 

If you require assistance setting up a Launchpad in some other architecture please open a Github Issue.

### Pixel and Launchpad

Once you have deployed your Launchpad instance the root url `/` should contain instructions for updating any front-end pixel code to leverage the launchpad instance.


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


# Development


## Run Locally:

*Prerequisites:*
* Java 11

*Steps:*

1. Clone the repo.
1. Start the local server: `./gradlew bootRun`
1. Open: [localhost:8080](http://localhost:8080)

## To Build the Docker Image:

*Prerequisites:*

* Java 11
* Docker

### Creating the Image

We utilize Google's Cloud Function Builder [buildpack](https://github.com/GoogleCloudPlatform/buildpacks) to ensure consistency with what will be generated on Google Cloud Functions

```
./gradlew bootBuildImage --builder=gcr.io/buildpacks/builder:v1 --imageName=snapchat-launchpad
```

After running that command you should have a docker image named `snapchat-launchpad`

This image can then be pushed to a repository with `docker push` or exported as a `.tar.gz` with `docker export`

### Running the container 

To run the built contianer, you may simply:

```
docker run --rm -p 8080:8080 snapchat-launchpad
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


## Multi-Party Compute (MPC)

Multi-Party Compute (MPC) is a Privacy Enhancing Technology that allows for multiple parties (in this context: the Launchpad instance and Snapchat Servers) to perform a computation such as an aggregation on sets of data in a cryptographically secure manner, such that neither party has the ability to read the underlying individual data of the other party.

This mode is more computationally expensive and provides less granularity than the default Relay mode. Please only use this mode if your use case requires it.

Fortunately on GCP or AWS, deploying Launchpad in MPC Mode is as easy as it is for Relay Mode.

### Deploying MPC Enabled Launchpad on Google CloudRun
[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://shell.cloud.google.com/?cloudshell_image=gcr.io/snap-launchpad-public/launchpad-cloud-shell/mpc:prod&show=terminal)

### Deploying MPC Enabled Launchpad on AWS CloudFormation
[![](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?#/stacks/create/template?stackName=snap-launchpad&templateURL=https://snap-launchpad-public.s3.amazonaws.com/cloudformation/template-mpc.yml)
