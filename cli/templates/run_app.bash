#!/bin/bash
echo "Sourcing deploy variables, if they're present..."
source deploy.env || true

found_container=$(docker ps -q -f name="${app}")
if [ "$found_container" ]; then
  echo "Stopping previous ${app} version..."
  docker stop ${app} --time ${stop_timeout}
fi

echo "Removing previous container...."
docker rm ${app}

echo
echo "Starting new ${app} version..."
echo

${pre_run_cmd}
${run_cmd}
${post_run_cmd}