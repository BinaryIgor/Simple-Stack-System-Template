#!/bin/bash
echo "Sourcing deploy variables, if they're present..."
source deploy.env || true

found_container=$(docker ps -q -f name="${app}")
if [ "$found_container" ]; then
  echo "Stopping previous ${app} version..."
  docker stop ${app} --time ${stop_timeout}
  timestamp=$(date +%s)

  if [ "${should_wait}" == "should_wait" ] && [ -f "${last_logs_collector_read_at_file}" ]; then
    for i in {1..5}
    do
      last_read_at=$(cat "${last_logs_collector_read_at_file}")
      if [ "$timestamp" \> "$last_read_at" ]; then
        echo "Waiting (5s) for last logs collection before removing container for $i/5 time"
        sleep 5
      else
        echo
        echo "Last logs collected!"
        break
      fi
    done
  fi
fi

echo "Removing previous container...."
docker rm ${app}

echo
echo "Starting new ${app} version..."
echo

${pre_run_cmd}
${run_cmd}
${post_run_cmd}