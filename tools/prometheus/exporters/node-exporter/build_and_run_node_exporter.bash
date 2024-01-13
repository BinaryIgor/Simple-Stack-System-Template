#!/bin/bash

docker rm node-exporter

docker build . -t node-exporter

docker run -p "9100:9100" --pid=host --name node-exporter node-exporter --path.rootfs=/host