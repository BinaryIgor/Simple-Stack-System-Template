#!/bin/bash

export LOCAL_PORT=11111
export REMOTE_PORT=11111

echo "Tunneling logs browser on a port: $LOCAL_PORT!"

exec bash setup_tunnel.bash