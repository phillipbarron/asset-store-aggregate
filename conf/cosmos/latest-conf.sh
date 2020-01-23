#!/bin/sh
# $1 is <path to your certificate>:<password>
curl --cert $1 https://api.live.bbc.co.uk/cosmos/env/int/component/optimo-history-api/configuration -o conf-optimo-history-api-int.json
curl --cert $1 https://api.live.bbc.co.uk/cosmos/env/test/component/optimo-history-api/configuration -o conf-optimo-history-api-test.json
curl --cert $1 https://api.live.bbc.co.uk/cosmos/env/live/component/optimo-history-api/configuration -o conf-optimo-history-api-live.json
