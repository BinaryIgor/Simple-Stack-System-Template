#!/bin/bash
set -eu

rm -f -r target
mkdir target
cp init_volume.bash target/init_volume.bash

envsubst < template_prometheus.yml > target/prometheus.yml 
envsubst '${INSTANCE_ALERT_TO_IGNORE_APPS_REGEX} ${LOGS_ERRORS_ALERT_TO_IGNORE_APPS_REGEX}' < template_alert_rules.yml > target/alert_rules.yml

cp -r target/* ${CI_PACKAGE_TARGET}
