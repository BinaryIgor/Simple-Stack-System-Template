#!/bin/bash

export LOCAL_PORT=5432
export LOCAL_PORT=5432

echo "Tunneling postgres on a port: $LOCAL_PORT!"

exec bash setup_tunnel.bash