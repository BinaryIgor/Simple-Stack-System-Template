#!/bin/bash

mkdir -p target/resources

./tailwindcss -i src/main/resources/resources/styles.css -o target/resources/live-styles.css --watch