#!/bin/bash
set -eu

rm -f -r target
mkdir target
cp init_volume.bash target/init_volume.bash

# TODO: remove
export ALERTMANAGER_PORT="9093"
export NODE_EXPORTER_PORT="9100"
export MONITOR_PORT="8080"
export INSTANCE_ALERT_TO_IGNORE_APPS_REGEX=".+(backup|-logs-browser)$"
export LOGS_ERRORS_ALERT_TO_IGNORE_APPS_REGEX=".+(-logs-browser)$"

envsubst < template_prometheus.yml > target/prometheus.yml 
envsubst '${INSTANCE_ALERT_TO_IGNORE_APPS_REGEX} ${LOGS_ERRORS_ALERT_TO_IGNORE_APPS_REGEX}' < template_alert_rules.yml > target/alert_rules.yml

#cp -r target/* ${CI_PACKAGE_TARGET}
