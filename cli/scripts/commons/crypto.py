import base64
import os
import secrets
import string

from cryptography.fernet import Fernet

from . import meta

KEY_BYTES_LENGTH = 32
PASSWORD_LENGTH = 48
PASSWORD_CHARACTERS = f'{string.ascii_letters}{string.digits}'


def random_key():
    return base64.b64encode(os.urandom(KEY_BYTES_LENGTH)).decode("ascii")


def random_password():
    characters = list(PASSWORD_CHARACTERS)
    return ''.join(secrets.choice(characters) for _ in range(PASSWORD_LENGTH))


def encrypted_data(password, data):
    key = _password_to_fernet_key(password)
    return Fernet(key).encrypt(data)


def _password_to_fernet_key(password):
    key = password
    lacking_key_chars = 32 - len(key)
    for _ in range(lacking_key_chars):
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
