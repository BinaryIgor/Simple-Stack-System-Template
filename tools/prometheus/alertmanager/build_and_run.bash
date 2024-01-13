#!/bin/bash

if [ -z "$slack_api_url" ]; then
    echo "slack_api_url is required, but it's not defined!"
    exit 1
fi

export slack_channel="${slack_channel:-monitoring}"

docker rm prometheus-am-exp

rm -r target
mkdir -p target

envsubst '${slack_api_url} ${slack_channel}' < alertmanager.yml > target/alertmanager.yml

docker build . -t prometheus-am-exp

#9093 port we have
docker run --network host --name prometheus-am-exp prometheus-am-exp