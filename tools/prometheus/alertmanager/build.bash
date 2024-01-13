#!/bin/bash
set -eu

rm -f -r target
mkdir target

envsubst '${WEBHOOK_RECEIVER_URL} ${EMAIL_RECEIVER_TO_EMAILS} ${EMAIL_RECEIVER_FROM_EMAIL} 
    ${EMAIL_RECEIVER_HOST} ${EMAIL_RECEIVER_USERNAME} ${EMAIL_RECEIVER_PASSWORD} ${EMAIL_REVEIVER_EMAIL_TAG}' < template_alertmanager.yml > target/alertmanager.yml 

cp -r target/* ${CI_PACKAGE_TARGET}
