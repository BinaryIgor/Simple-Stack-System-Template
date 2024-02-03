#!/bin/bash
set -e

cwd=$PWD

echo "Building..."
python build_app.py --env prod --app grafana
echo
echo "Running..."
cd ../target/grafana
bash load_and_run_app.bash

echo
echo "Setting up tunnels.."
cd $cwd/tunnels
export TUNNELS="prometheus postgres"
bash setup_tunnels.bash

echo
echo "Stopping grafana..."
docker stop grafana
echo
echo "Grafana stopped, tunnels closed"