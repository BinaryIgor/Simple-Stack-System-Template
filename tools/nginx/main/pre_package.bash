#!/bin/bash
set -eu

rm -f -r deploy
mkdir deploy
mkdir deploy/conf

export APP_URL="${APP_URL:-http://0.0.0.0:9999}"

envsubst '${HTTP_PORT} ${HTTPS_PORT} ${DOMAIN} ${PRIVATE_IP_RANGE} ${APP_URL}' < template_nginx.conf > deploy/conf/default.conf
envsubst '${HTTP_PORT} ${HTTPS_PORT} ${DOMAIN} ${PRIVATE_IP_RANGE}' < template_nginx.conf > deploy/template_nginx_app.conf

if [ $CI_ENV == 'local' ]; then
    cp -r "${CI_REPO_ROOT_PATH}/cli/fake-certs" deploy/fake-certs
fi

export nginx_container="nginx-api-gateway"
# check if both proxying and proxied app are working properly
export app_health_check_url="${APP_HEALTH_CHECK_URL}"

envsubst '${nginx_container} ${app_health_check_url}' < template_update_app_url.bash > deploy/update_app_url.bash

cp update_app_url_pre_start.bash deploy/update_app_url_pre_start.bash

envsubst '${app_health_check_url}' < template_check_proxied_app.bash > deploy/check_proxied_app.bash

envsubst '${nginx_container}' < template_reload_nginx_config.sh > deploy/reload_nginx_config.sh