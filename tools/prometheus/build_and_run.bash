#!/bin/bash

container="simple-stack-system-template-volume"

docker rm $container

docker build . -t $container

docker volume create "$container-volume"

#9090 port we have
#--log-driver=fluentd --log-opt tag="docker.{{.ID}}" --log-opt labels="instance-id" \
docker run \
  --network host -v "$container-volume:/prometheus" \
  --name $container $container  \
  --storage.tsdb.retention.time=30d  \
  --config.file=/etc/prometheus/prometheus.yml