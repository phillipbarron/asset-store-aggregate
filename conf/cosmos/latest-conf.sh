#!/bin/sh
# $1 is <path to your certificate>:<password>
curl --cert $1 https://api.live.bbc.co.uk/cosmos/env/int/component/asset-store-aggregate/configuration -o conf-asset-store-aggregate-int.json
curl --cert $1 https://api.live.bbc.co.uk/cosmos/env/test/component/asset-store-aggregate/configuration -o conf-asset-store-aggregate-test.json
curl --cert $1 https://api.live.bbc.co.uk/cosmos/env/live/component/asset-store-aggregate/configuration -o conf-asset-store-aggregate-live.json
