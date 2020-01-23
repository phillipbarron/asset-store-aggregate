#!/bin/sh
echo "Merging Swagger paths ..."
jq -s '.' paths/*.json | jq 'add' > paths.json
jq -s '.' metadata.json paths.json | jq '.[0].paths = .[1] | .[0]' > api.json
rm paths.json
