from os import path

from commons import meta, crypto

PASSWORD = "password"
KEY = "key"

args = meta.cmd_args({
    "name": {
        "help": "secret name",
        "required": True
    },
    "value": {
        "help": "Value of the secret. Random value if not given"
    },
    "type": {
        "help": f"Type of the secret. Either {PASSWORD} or a {KEY}"
    }
}, script_description="Script to store given or generated secret")

log = meta.new_log("store_secret")

name = args["name"]

log.info(f"About to store {name} secret...")

value = args["value"]
secret_type = args["type"]
if not value:
    if not secret_type:
        raise Exception("Secret type is required when value is not given!")

    log.info("Value not given, generating random one...")
    value = crypto.random_key() if secret_type == KEY else crypto.random_password()

password = input(f"{name} password:")
encrypted = crypto.encrypted_data(password, value.encode("utf-8"))

secret_file = path.join(meta.cli_secrets_dir(), name)

meta.write_binary_to_file(secret_file, encrypted)
