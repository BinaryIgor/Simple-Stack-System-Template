#!/bin/bash
set -e

BUILD_COMMONS=${CI_BUILD_COMMONS:-true}
SKIP_COMMONS_TESTS="${CI_SKIP_COMMONS_TESTS:-false}"
SKIP_TESTS="${CI_SKIP_TESTS:-false}"
EMAIL_TEMPLATES_PATH="${CI_REPO_ROOT_PATH}/email-templates"

if [ $BUILD_COMMONS == "true" ]; then
  app_dir=$PWD
  cd ../../
  mvn clean install
  cd commons
  if [ $SKIP_COMMONS_TESTS == "true" ] || [ $SKIP_TESTS == "true" ]; then
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

cp -r $EMAIL_TEMPLATES_PATH deploy/email-templates

echo "Copying static files..."
mkdir -p deploy/static
cp -r static/messages deploy/static/messages
cp -r static/templates deploy/static/templates

echo
echo "Building css, index.html and other dynamic assets..."
cd static
bash build_bundle.bash
cd ..
echo

if [ $CI_ENV == "local" ]; then
  HTTP_PORT=8080
else
  HTTP_PORT=$(shuf -i 10000-20000 -n 1)
fi

echo "http://0.0.0.0:$HTTP_PORT" > "$APP_URL_FILE"
echo "$HTTP_PORT" > "$APP_PORT_FILE"
