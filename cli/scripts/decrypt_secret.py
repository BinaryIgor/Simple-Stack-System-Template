from os import path

from commons import meta, crypto

args = meta.cmd_args({
    "name": {
        "help": "secret name",
        "required": True
    },
}, script_description="Script to decrypt secret and print it to the stdout")

log = meta.new_log("decrypt_secret")

name = args["name"]

password = input(f"{name} password:")
secret_file = path.join(meta.cli_secrets_dir(), name)
decrypted = crypto.decrypted_file(secret_file, password)

print(decrypted)
