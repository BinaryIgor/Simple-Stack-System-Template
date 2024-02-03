from commons import meta, crypto

args = meta.cmd_args({
    "password": {
        "action": "store_true"
    }
}, env_arg=False, script_description="Script to store given or generated secret")

value = crypto.random_password(length=32) if args["password"] else crypto.random_key()
print(value)
