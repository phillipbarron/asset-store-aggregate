#!/bin/bash

set -e

UNIQUE_SUFFIX="test-asset-store-aggregate"

# These must be tags that exist in the docker library
POSTGRES_VERSION=9.6

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
        --start-postgres )
            docker run -d --name "postgres-${UNIQUE_SUFFIX}" -d -p 5432:5432 postgres:${POSTGRES_VERSION}
            ;;
        --wait-postgres )
            retryPort 5432
            ;;
        --logs-postgres )
            docker logs "postgres-${UNIQUE_SUFFIX}"
            ;;
        --stop-and-remove )
            docker rm -f "postgres-${UNIQUE_SUFFIX}"
            ;;
        * )
        echo "Unexpected $1"
        exit 1
    esac
    shift
done
