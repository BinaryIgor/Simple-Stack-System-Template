CREATE SCHEMA "user";

CREATE TABLE "user"."user" (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    language TEXT NOT NULL,
    state TEXT NOT NULL,
    roles TEXT[] NOT NULL,
    password TEXT NOT NULL,
    second_factor_auth BOOLEAN NOT NULL
);