import psycopg2

from . import meta, crypto

log = meta.new_log("db")


def root_connection(root_user=None, root_password=None, db_name=None):
    env_config = meta.env_config()
    db_host = env_config["db-local-host"]
    db_port = env_config["db-port"]

    if root_user is None:
        root_user = env_config["db-root-user"]

    if root_password is None:
        root_password = crypto.decrypted_secret("db-root-password", "internal", root_password)

    conn = psycopg2.connect(host=db_host,
                            port=db_port,
                            db_name=db_name if db_name else env_config["db-root-name"],
                            user=root_user,
                            password=root_password,
                            connect_timeout=3)

    conn.autocommit = True

    return conn


def create_db_if_does_not_exist(cursor, db):
    log.info(f"Creating {db} db...")

    cursor.execute(f"SELECT * FROM pg_database WHERE datname='{db}'")

    res = cursor.fetchone()
    if res:
        log.info(f"{db} already exists, skipping")
    else:
        cursor.execute(f'CREATE DATABASE "{db}"')


def create_user_if_does_not_exist(cursor, user, password, db, privileges=None, conn_limit=None):
    log.info(f"Creating {user} user...")

    cursor.execute(f"SELECT 1 FROM pg_roles WHERE rolname='{user}'")

    res = cursor.fetchone()
    if res:
        log.info(f"{user} user already exists, skipping")
    else:
        cursor.execute(f'CREATE USER "{user}" WITH PASSWORD \'{password}\'')

    cursor.execute(f'GRANT CONNECT ON DATABASE "{db}" TO "{user}"')

    if privileges:
        log.info(f"Granting {privileges} privileges on {db} db to {user}")
        cursor.execute(f'GRANT {privileges} ON DATABASE "{db}" to "{user}"')

    if conn_limit:
        cursor.execute(f'ALTER USER "{user}" WITH CONNECTION LIMIT {conn_limit}')


def grant_read_privileges(cursor, user, db, schemas=None):
    if schemas is None:
        schemas = ['public']
    elif schemas == 'all':
        cursor.execute("""
        SELECT schema_name FROM information_schema.schemata
        WHERE schema_name NOT like 'pg_%';
        """)
        schemas = [r[0] for r in cursor.fetchall()]

    cursor.execute(f'GRANT CONNECT ON DATABASE "{db}" TO "{user}"')

    for s in schemas:
        log.info(f"Granting read privileges to {user} on schema {s}")
        try:
            cursor.execute(f"""
            GRANT USAGE ON SCHEMA "{s}" TO "{user}";
            GRANT SELECT ON ALL TABLES IN SCHEMA "{s}" TO "{user}";
            """)
        except Exception:
            log.exception(f"Failed to grant permissions on {s} schema for {user}")


def alter_user_password(cursor, user, password):
    cursor.execute(f"ALTER USER {user} WITH PASSWORD '{password}'")
