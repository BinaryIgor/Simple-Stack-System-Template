#!/bin/bash

cd static
npx tailwindcss -i assets/styles.css -o ../deploy/static/assets/styles.css --minify
