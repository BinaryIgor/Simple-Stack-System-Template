#!/bin/bash

export LOCAL_PORT=9090
export REMOTE_PORT=9090

echo "Tunneling Prometheus on a port: $LOCAL_PORT!"

exec bash setup_tunnel.bash