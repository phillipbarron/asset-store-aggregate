Asset Store Aggregate
==================

An attempt to build an aggregae from the events in the Optimo Event store which will serve an API much like the Optimo Asset Store

To run application:

    ./run-local.sh

##### Note re. the S3 client
When running locally the snapshot repository (i.e. AWS S3) is faked using a local dummy client that holds data in an
in-memory hashmap. If you wish to run locally but use the real AWS S3, make sure that your are logged into AWS on the command line then pass this JVM parameter when starting:


    sbt -DuseRealS3Client=true run

## Tests

To run unit tests

    sbt test

To run cucumber tests (you need to [install Docker](https://docs.docker.com/docker-for-mac/install/)):

    ./ci/run-cucumber.sh



### This service was created from [cps-scalatra-skeleton](https://github.com/bbc/cps-scalatra-skeleton)

The script to compile jar, build RPM and release to Cosmos is at ```./ci/build.sh```.  Use of the ```ci``` folder will make it simpler to setup a CPS pipeline.

Your Jenkins RPM job should run this script from the ```ci``` directory or else various paths will fail.

Other non-service-specific files (e.g. spec file and bake scripts) are in the ```infrastructure``` directory.

### Whitelisting

This service is whitelisted and is hosted in https://github.com/bbc/cam-whitelisting.

The whitelist is disabled on int, Jenkins and locally.
