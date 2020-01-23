#!/bin/bash

set -e

UNIQUE_SUFFIX="test-asset-store-aggregate"

# These must be tags that exist in the docker library
MONGO_VERSION=4.2.2-bionic

function retryPort() {
    local port=$1
    local n=0

    until [ $n -ge 30 ]; do
        set +e
        nc -v -w 1 localhost ${port} </dev/null
        returnCode=$?

        set -e
        if [ ${returnCode} -eq  '0' ]; then
            echo "Connection to ${port} is available"
            exit 0;
        else
            n=$(($n + 1))
            echo "${n} - retrying connection to port ${port}";
            sleep 1
        fi
    done
}

while [ "$1" != "" ]; do
    case $1 in
        --start-mongo )
            docker run -d --name "mongo-${UNIQUE_SUFFIX}" -d -p 27017:27017 mongo:${MONGO_VERSION}
            ;;
        --wait-mongo )
            retryPort 27017
            ;;
        --logs-mongo )
            docker logs "mongo-${UNIQUE_SUFFIX}"
            ;;
        --stop-and-remove )
            docker rm -f "mongo-${UNIQUE_SUFFIX}"
            ;;
        * )
        echo "Unexpected $1"
        exit 1
    esac
    shift
done
