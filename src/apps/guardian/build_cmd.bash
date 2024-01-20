#!/bin/bash
set -e

BUILD_COMMONS=${CI_BUILD_COMMONS:-true}
SKIP_TESTS="${CI_SKIP_TESTS:-false}"

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
