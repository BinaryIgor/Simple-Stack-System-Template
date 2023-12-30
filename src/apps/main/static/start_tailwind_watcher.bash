#!/bin/bash

mkdir -p target/assets

./tailwindcss -i assets/styles.css -o target/assets/live-styles.css --watch