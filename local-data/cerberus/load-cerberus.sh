#!/bin/bash
set -x

host='http://localhost:5006'
appName='demo'

curl -v \
    -X POST \
    -H "Content-Type: application/json" \
    --data @local-data/cerberus/config.json \
  $host/v1/secret/app/$appName/config

curl -v \
    -X POST \
    -H "Content-Type: application/json" \
    --data "$(cat local-data/cerberus/config.json)" \
  $host/v1/secret/app/$appName/config

set +x
