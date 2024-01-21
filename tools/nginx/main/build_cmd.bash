#!/bin/bash
set -eu

rm -f -r target
mkdir target
mkdir target/conf

export APP_URL="${APP_URL:-http://0.0.0.0:9999}"

envsubst '${HTTP_PORT} ${HTTPS_PORT} ${DOMAIN} ${PRIVATE_IP_RANGE} ${APP_URL}' < template_nginx.conf > target/conf/default.conf
envsubst '${HTTP_PORT} ${HTTPS_PORT} ${DOMAIN} ${PRIVATE_IP_RANGE}' < template_nginx.conf > target/template_nginx_app.conf

if [ $CI_ENV == 'local' ]; then
    cp -r "${CI_REPO_ROOT_PATH}/cli/config/fake-certs" target/fake-certs
fi

export nginx_container="nginx-main"
# check if both proxying and proxied app are working properly
# TODO: better place?
export app_health_check_url="http://0.0.0.0:8081/actuator/health"

envsubst '${nginx_container} ${app_health_check_url}' < template_update_app_url.bash > target/update_app_url.bash

cp update_app_url_pre_start.bash target/update_app_url_pre_start.bash

envsubst '${app_health_check_url}' < template_check_proxied_app.bash > target/check_proxied_app.bash

envsubst '${nginx_container}' < template_reload_nginx_config.sh > target/reload_nginx_config.sh

cp -r target/* ${CI_PACKAGE_TARGET}