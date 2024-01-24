from os import path

from commons import meta, crypto

PASSWORD = "password"
KEY = "key"

args = meta.cmd_args({
    "name": {
        "help": "secret name",
        "required": True
    },
    "type": {
        "help": f"Type of the secret. Either {PASSWORD} or a {KEY}"
    }
}, script_description="Script to store given or generated secret")

log = meta.new_log("store_secret")

name = args["name"]

log.info(f"About to store {name} secret...")

secret_type = args["type"]
if secret_type:
    log.info("Value not given, generating random one...")
    value = crypto.random_key() if secret_type == KEY else crypto.random_password()
else:
    value = input(f"{name} value: ")

password = input(f"{name} password: ")
encrypted = crypto.encrypted_data(password, value.encode("utf-8"))

secret_file = path.join(meta.cli_secrets_dir(), name)

meta.write_binary_to_file(secret_file, encrypted)
