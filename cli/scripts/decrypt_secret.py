from commons import meta, crypto

args = meta.cmd_args({
    "name": {
        "help": "secret name",
        "required": True
    },
    "group": {
        "help": "secret group",
        "required": True
    },
}, script_description="Script to decrypt secret and print it to the stdout")

log = meta.new_log("decrypt_secret")

name = args["name"]
group = args["group"]

password = input(f"{group} group password:")
decrypted = crypto.decrypted_secret(key=name, group=group, encryption_password=password)

print(decrypted)
