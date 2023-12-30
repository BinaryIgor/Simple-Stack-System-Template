#!/bin/bash

docker build . -t simple-stack-system-template-postgres-client

docker rm simple-stack-system-template-postgres-client

export PGDATABASE="${DB_NAME:-postgres}"
export PGHOST="${DB_HOST:-0.0.0.0}"
export PGPORT="${DB_PORT:-5432}"
export PGUSER="${DB_USER:-postgres}"
export PGPASSWORD="${DB_PASSWORD:-postgres}"

exec docker run -it --network host -e "PGDATABASE" -e "PGHOST" -e "PGPORT" -e "PGUSER" -e "PGPASSWORD" --name simple-stack-system-template-postgres-client simple-stack-system-template-postgres-client
