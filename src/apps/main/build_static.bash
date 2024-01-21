#!/bin/bash
set -e

echo "Copying static files..."
mkdir -p target/static
rm -r target/static/*
cp -r static/messages target/static/messages
cp -r static/templates target/static/templates

echo
echo "Building css, index.html and other dynamic assets..."
cd static
bash build_bundle.bash