#!/bin/bash
tunnels=($TUNNELS)
pid_file="/tmp/setup_tunnel.pid"

echo
echo "Setting up $tunnels tunnels, in the background..."
echo

set +e
trap break INT

idx=0
for t in ${tunnels[@]};
do
  echo "Setting $t tunnel..."
  bash "setup_${t}_tunnel.bash"

  t_pid=$(cat $pid_file)
  t_pids[$idx]=$t_pid
  idx=$((idx+1))
done

while true;
do
   echo "Tunnels: waiting for close command"
   echo "..."
   sleep 300
   echo
done

trap - INT
sleep 1

echo "Closing tunnels..."
for t in ${t_pids[@]};
do
  echo "Killing..$t"
  kill $t
done
echo "Tunnels closed"