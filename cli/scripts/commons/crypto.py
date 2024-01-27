import base64
import os
import secrets
import string
import json

from os import path

from cryptography.fernet import Fernet

from . import meta

KEY_BYTES_LENGTH = 32
PASSWORD_LENGTH = 48
PASSWORD_CHARACTERS = f'{string.ascii_letters}{string.digits}'

SECRETS_GROUPS = "groups"
SECRETS_DATA = "data"

log = meta.new_log("crypto")


def random_key():
    return base64.b64encode(os.urandom(KEY_BYTES_LENGTH)).decode("ascii")


def random_password():
    characters = list(PASSWORD_CHARACTERS)
    return ''.join(secrets.choice(characters) for _ in range(PASSWORD_LENGTH))


def secrets_file():
    s_file_path = _secrets_file_path()
    if path.exists(s_file_path):
        return json.loads(meta.file_content(s_file_path))
    return {SECRETS_GROUPS: {}, SECRETS_DATA: {}}


def _secrets_file_path():
    return path.join(meta.cli_config_dir(), meta.current_env(), "secrets.json")


def save_secret(key, value, group, encryption_password):
    file = secrets_file()
    groups = file[SECRETS_GROUPS]
    if group in groups:
        group_secrets = groups[group]
        if key not in group_secrets:
            log.info("Group didn't have this particular secret, adding it")
            group_secrets.append(key)

        log.info(f"Group: {group} exists already, decrypting previous values...")
        decrypted_group = _decrypted_group_from_secrets_file(file, group, encryption_password)
        log.info(f"Group decrypted, storing new {key} value there")
        decrypted_group[key] = value
        decrypted_group_bytes = json.dumps(decrypted_group).encode("utf8")

        new_encrypted_group = encrypted_data(decrypted_group_bytes, encryption_password)
        new_encrypted_group_base64 = base64.b64encode(new_encrypted_group).decode("ascii")

        file[SECRETS_DATA][group] = new_encrypted_group_base64
    else:
        log.info(f"Group: {group} doesn't exist, adding it...")
        groups[group] = [key]
        decrypted_group = {key: value}
        decrypted_group_bytes = json.dumps(decrypted_group).encode("utf8")
        encrypted_group = encrypted_data(decrypted_group_bytes, encryption_password)
        encrypted_group_base64 = base64.b64encode(encrypted_group).decode("ascii")

        file[SECRETS_DATA][group] = encrypted_group_base64

    meta.write_to_file(_secrets_file_path(), json.dumps(file, indent=2))


def _decrypted_group_from_secrets_file(file, group, encryption_password):
    encrypted_group = file[SECRETS_DATA][group]
    decrypted_group_data = decrypted_data(base64.b64decode(encrypted_group), encryption_password)
    return json.loads(decrypted_group_data)


def decrypted_secret(key, group, encryption_password):
    file = secrets_file()
    groups = file[SECRETS_GROUPS]
    if group not in groups:
        raise Exception(f"{group} doesn't exist in secret file")

    decrypted_group = _decrypted_group_from_secrets_file(file, group, encryption_password)

    if key not in decrypted_group:
        raise Exception(f"There is no {key} in group secrets. Available: {list(decrypted_group.keys())}")

    return decrypted_group[key]


def encrypted_data(data, password):
    key = _password_to_fernet_key(password)
    return Fernet(key).encrypt(data)


def _password_to_fernet_key(password):
    key = password
    lacking_key_chars = 32 - len(key)
    for i in range(lacking_key_chars):
        key += '0'

    return base64.urlsafe_b64encode(key.encode('utf8'))


def decrypted_data(data, password=None):
    password = _given_or_from_input_password(password)
    key = _password_to_fernet_key(password)
    return Fernet(key).decrypt(data)


def _given_or_from_input_password(password):
    if password is None:
        password = input("Secrets password")
    return password


def decrypted_file(file_path, password):
    key = _password_to_fernet_key(password)
    data = meta.binary_file_content(file_path)
    return Fernet(key).decrypt(data).decode("utf8")
