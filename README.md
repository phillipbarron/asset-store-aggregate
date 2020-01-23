Asset Store Aggregate
==================

An attempt to build an aggregate from the events in the Optimo Event store which will serve an API much like the READ (ie GET) Optimo Asset Store (**eventually**)

To run application:

    ./run-local.sh

Todo:

* Create case class for both working and published branches
* Consume massage from a Kafka Topic
* Add a Mongo Dao with CRUD API
* Add a mongo docker container for testing / local dev
* Integrate docker mongo with startup script
* Add ``GET /assets/{id}`` endpoint (What branch should that default to?)
* Add ``GET /assets/{id}/branch/{branch} `` endpoint



### Whitelisting

This service is whitelisted and is hosted in https://github.com/bbc/cam-whitelisting.

The whitelist is disabled on int, Jenkins and locally.
