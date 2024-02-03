from commons import meta, crypto

PASSWORD = "password"
KEY = "key"

args = meta.cmd_args({
    "name": {
        "help": "secret name",
        "required": True
    },
    "group": {
        "help": "secret group",
        "required": True
    },
    "type": {
        "help": f"Type of the secret. Either {PASSWORD} or a {KEY}"
    }
}, script_description="Script to store given or generated secret")

log = meta.new_log("store_secret")

name = args["name"]
group = args["group"]

log.info(f"About to store {name} secret in {group}...")

secret_type = args["type"]
if secret_type:
    log.info("Value not given, generating random one...")
    value = crypto.random_key() if secret_type == KEY else crypto.random_password()
else:
    value = crypto.secret_input(f"{name} value: ")

password = crypto.secret_input(f"{group} group password: ")

secrets_file = crypto.secrets_file()

crypto.save_secret(key=name, value=value, group=group, encryption_password=password)

log.info(f"Secret {name} saved in the {group}")

