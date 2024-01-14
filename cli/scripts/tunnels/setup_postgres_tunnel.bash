#!/bin/bash

export LOCAL_PORT=5432
export REMOTE_PORT=25060
export REMOTE_HOST="db-host"

echo "Tunneling postgres on a port: $LOCAL_PORT!"

exec bash setup_tunnel.bash