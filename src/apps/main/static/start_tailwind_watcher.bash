#!/bin/bash

mkdir -p target/assets

npx tailwindcss -i assets/styles.css -o target/assets/live-styles.css --watch