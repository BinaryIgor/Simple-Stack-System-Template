#!/bin/bash

mkdir -p target/assets

live_css_path=target/assets/live-styles.css
export stylesPath=$live_css_path
export indexJsPath="/index.js"
export componentsPath="/components.js"

envsubst '$stylesPath $indexJsPath $componentsPath' < templates/template_index.html > templates/index.html

npx tailwindcss -i assets/styles.css -o $live_css_path --watch