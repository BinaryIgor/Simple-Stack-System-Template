#!/bin/bash
loki_container="loki"
promtail_container="promtail"

docker stop $promtail_container
docker stop $loki_container

docker rm $loki_container
docker rm $promtail_container

docker run --name $loki_container -d -v $(pwd):/mnt/config -p 3100:3100 grafana/loki:2.9.1 -config.file=/mnt/config/loki-config.yaml
docker run --network host --name $promtail_container -d -v $(pwd):/mnt/config \
  -v "/var/run/docker.sock:/var/run/docker.sock" \
  -v /var/log:/var/log grafana/promtail:2.9.1 \
  -config.file=/mnt/config/promtail-config.yaml