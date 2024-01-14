#!/bin/bash
set -euo pipefail

local_port=${LOCAL_PORT:-9090}
remote_port=${REMOTE_PORT:-9090}
#TODO: generate it/add to readme
host=${HOST:-"68.183.73.126"}
remote_host=${REMOTE_HOST:-"0.0.0.0"}
remote_user=${REMOTE_USER:-'simple-stack-system-template'}
pid_file="/tmp/setup_tunnel.pid"

ssh -o StrictHostKeyChecking=accept-new -N -L $local_port:$remote_host:$remote_port $remote_user@$host &
t_pid=$!
echo "Waiting to establish a tunnel..."
echo $t_pid > $pid_file
sleep 1
echo "Tunnel pid: $t_pid"
