# Simple Stack System Template CLI

CLI to automate all kinds of operations.

## Requirements

* non-root docker access for current user
* bash interpreter
* python >= 3.9

## Run

### Setup:

```
bash setup_cli.bash
```

### Activate (always before running scripts):

```
source venv/bin/activate
cd src
```

Always run scripts from **src dir**, they might not work otherwise.

Additionally, only for prod/another remote env:

```
export COLLYBRI_CLI_SECRETS_PASSWORD=<password for encrypted secrets file, expected under path: $HOME/.collybri-cli/secrets>
```

For local env, secrets are taken from config/local-secrets as plain text. Use appropriate argument for scripts, for
example:

```
python3 build_app.py --env prod --app prometheus
```

## Running system locally
Just do
```
export STATIC_FILES_PATH=/some-path-with-frontend package (optional)
bash run_system_locally.bash --build (if you need to build new images)
```
For swagger go to: `http://localhost:8080/swagger-ui.html`

## Logs
TODO

## TODO
* secrets container: encrypted secrets on the disk, one container decrypts them, others use their volume
* secrets: one file/multiple files encrypted with different secrets? Secret groups?
* logs solution
* Do not remove previous (backup) container for logs gathering/collection!
* remove previous versioned assets (keep only a few recent ones)
* simplify collector
  * export metrics directly to prom
  * save files locally/push to sth simpler
  * https://prometheus.github.io/client_python/getting-started/three-step-demo/