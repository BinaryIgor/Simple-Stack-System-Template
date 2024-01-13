#!/bin/bash

docker rm simple-stack-system-template-grafana

docker build . -t simple-stack-system-template-grafana

docker run --network host --name simple-stack-system-template-grafana simple-stack-system-template-grafana