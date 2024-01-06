#!/bin/bash

cd static
./tailwindcss -i assets/styles.css -o ../deploy/static/assets/styles.css --minify
