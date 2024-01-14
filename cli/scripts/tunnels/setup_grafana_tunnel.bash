#!/bin/bash

export LOCAL_PORT=3000
export REMOTE_PORT=3000

echo "Tunneling Grafana on a port: $LOCAL_PORT!"

exec bash setup_tunnel.bash