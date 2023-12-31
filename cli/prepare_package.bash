#!/bin/bash
# a: exports all variables to the environment from .env files
set -ea

export CI_ENV=${ENV:-local}

if [ -z $APP_DIR ]; then
  echo "APP_DIR is required!"
  exit 1
fi

. "ci_config_${CI_ENV}.env"

scripts_dir=${PWD}
cd ..
cd ${APP_DIR}

ci_config_file="ci_config_${CI_ENV}.env"
if [ ! -e $ci_config_file ]; then
  echo "Required $ci_config_file file doesn't exist!"
  exit 1
fi
. $ci_config_file

tag="${TAG:-latest}"
tagged_image="${APP}:${tag}"

echo "Creating package in target directory for $tagged_image image..."
echo "Preparing deploy dir in $APP_DIR.."

rm -r -f deploy
mkdir deploy

echo "Building image..."

if [ -n "${PRE_PACKAGE_SCRIPT}" ]; then
    echo "Running pre $PRE_PACKAGE_SCRIPT package script.."
    bash ${PRE_PACKAGE_SCRIPT}
fi

docker build . -t ${tagged_image}

gzipped_image_path="deploy/$APP.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

export app=$APP
export tag=$tag

export pre_run_cmd="${PRE_RUN_CMD:-}"

extra_run_args="${EXTRA_RUN_ARGS:-}"
if [ -n "${EXTRA_RUN_ARGS2}" ]; then
  extra_run_args="$extra_run_args $EXTRA_RUN_ARGS2"
fi
if [ -n "${EXTRA_RUN_ARGS3}" ]; then
  extra_run_args="$extra_run_args $EXTRA_RUN_ARGS3"
fi

if [ $CI_ENV == "local" ]; then
  restart_arg="--restart no"
else
  restart_arg="--restart unless-stopped"
fi

export run_cmd="docker run -d $extra_run_args $restart_arg --name $app $tagged_image"

export post_run_cmd="${POST_RUN_CMD:-}"

envsubst '${app} ${tag}' < "$scripts_dir/template_load_and_run_app.bash" > deploy/load_and_run_app.bash

if [ -n "${ZERO_DOWNTIME_DEPLOY}" ]; then
  export app_url=$(cat ${APP_URL_FILE})
  export nginx_dir=${ZERO_DOWNTIME_NGINX_DIR}
  envsubst '${app} ${pre_run_cmd} ${run_cmd} ${post_run_cmd} ${app_url} ${nginx_dir}' \
    < "$scripts_dir/template_run_zero_downtime_app.bash" > deploy/run_app.bash
else
  envsubst '${app} ${pre_run_cmd} ${run_cmd} ${post_run_cmd}' < "$scripts_dir/template_run_app.bash" > deploy/run_app.bash
fi

echo "Package prepared."