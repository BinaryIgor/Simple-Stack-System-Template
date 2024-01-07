#!/bin/bash

bundle_root=../deploy/static
bundle_dir=$bundle_root/assets
bundle_hash=$(date +%s)
css_bundle="styles_$bundle_hash.css"
index_js_bundle="index_$bundle_hash.js"
components_bundle="components_$bundle_hash.js"

npx tailwindcss -i assets/styles.css -o "$bundle_dir/$css_bundle" --minify

# TODO: minify
npx rollup assets/components.js --file "$bundle_dir/$components_bundle"
npx rollup assets/index.js --file "$bundle_dir/$index_js_bundle"

cp -r assets/lib "$bundle_dir/lib/"

export stylesPath=$css_bundle
export indexJsPath=$index_js_bundle
export componentsPath=$components_bundle
envsubst '$stylesPath $indexJsPath $componentsPath' < templates/template_index.html > $bundle_root/templates/index.html