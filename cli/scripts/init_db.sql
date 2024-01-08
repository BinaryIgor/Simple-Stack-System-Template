CREATE DATABASE "simple-stack-system-template";

CREATE USER "simple-stack-system-template" WITH password 'simple-stack-system-template';
GRANT ALL PRIVILEGES ON DATABASE "simple-stack-system-template" TO "simple-stack-system-template";
\c 'simple-stack-system-template';
GRANT ALL ON SCHEMA public TO "simple-stack-system-template";

