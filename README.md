Asset Store Aggregate
==================

An attempt to build an aggregate from the events in the Optimo Event store which will serve an API much like the READ (ie GET) Optimo Asset Store (**eventually**)

To run application:

    ./run-local.sh

Todo:

* Create case class for both working and published branches **&#9745;**
* Consume messages from a Kafka Topic **&#9745;**
* Add a Mongo DAO with CRUD API
* Add a Mongo Docker container for testing / local dev **&#9745;** 
* Integrate Docker/Mongo with startup script **&#9745;**
* Add ``GET /assets/{id}`` endpoint (What branch should that default to?)
* Add ``GET /assets/{id}/branch/{branch} `` endpoint
* Add mechanism to trash DB a re-consume queue from scratch
* Add index to db ```db.assets.createIndex( { "assetId": 1 }, { unique: true } )```


### Whitelisting

This service is whitelisted and is hosted in https://github.com/bbc/cam-whitelisting.

The whitelist is disabled on int, Jenkins and locally.
