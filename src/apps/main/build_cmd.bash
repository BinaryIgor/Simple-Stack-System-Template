#!/bin/bash
set -e

BUILD_COMMONS=${CI_BUILD_COMMONS:-true}
SKIP_TESTS="${CI_SKIP_TESTS:-false}"
EMAIL_TEMPLATES_PATH="${CI_REPO_ROOT_PATH}/email-templates"

if [ $BUILD_COMMONS == "true" ]; then
  app_dir=$PWD
  cd ../../
  mvn clean install
  cd commons
  if [ $SKIP_TESTS == "true" ]; then
    mvn clean install -Dmaven.test.skip=true
  else
    mvn clean install -PallTests
  fi
  cd $app_dir
fi

if [ $SKIP_TESTS == "true" ]; then
  mvn clean install -Dmaven.test.skip=true
else
  mvn clean install -PallTests
fi

cp -r $EMAIL_TEMPLATES_PATH target/email-templates

bash build_static.bash
echo

export target_dir="${CI_DEPLOY_STATIC_PATH}"
envsubst '${target_dir}' < template_copy_versioned_assets.bash > "$CI_PACKAGE_TARGET/copy_versioned_assets.bash"


HTTP_PORT=$(shuf -i 10000-20000 -n 1)

echo "http://0.0.0.0:$HTTP_PORT" > "$CI_PACKAGE_TARGET/$APP_URL_FILE"
echo "$HTTP_PORT" > "$CI_PACKAGE_TARGET/$APP_PORT_FILE"

cp -r $EMAIL_TEMPLATES_PATH "$CI_PACKAGE_TARGET/email-templates"
cp -r target/static "$CI_PACKAGE_TARGET/static"
