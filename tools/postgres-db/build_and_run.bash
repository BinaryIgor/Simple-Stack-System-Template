#!/bin/bash

docker build . -t simple-stack-system-template-postgres-db

docker rm simple-stack-system-template-postgres-db

exec docker run -p "5432:5432" --name simple-stack-system-template-postgres-db simple-stack-system-template-postgres-db
