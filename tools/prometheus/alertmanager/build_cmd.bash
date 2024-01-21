#!/bin/bash
set -eu

rm -f -r target
mkdir target

envsubst '${WEBHOOK_RECEIVER_URL}' < template_alertmanager.yml > target/alertmanager.yml

cp -r target/* ${CI_PACKAGE_TARGET}
