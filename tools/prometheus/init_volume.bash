#!/bin/bash
set -eu

volume_exists=$(docker volume inspect ${1})
if [ -z "$volume_exists" ]; then
    echo "Creating $1 volume..."
    docker volume create ${1}
else
    echo "$1 volume exists, no need to create it!"
fi