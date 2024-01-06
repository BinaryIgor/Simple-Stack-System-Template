#!/bin/bash
set -e

export CI_REPO_ROOT_PATH="../../.."
export CI_ENV=local
export CI_PACKAGE_TARGET=dist

mkdir -p $CI_PACKAGE_TARGET

export APP_URL_FILE=app_url.txt
export APP_SERVER_PORT_FILE=app_server_port.txt

bash build_cmd.bash