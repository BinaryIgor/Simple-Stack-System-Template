from commons import meta, task_deploy_app

args = meta.cmd_args(args_definition={
    "app": {
        "help": "Name of the app",
        "required": True
    },
    "copy_only": {
        "help": "Only copy new app package, do not run it. Useful for special cases/tests/debugging",
        "action": "store_true"
    },
    "local": {
        "help": "Execute local deployment for debugging purposes mainly",
        "action": "store_true"
    }
}, prod_env=True, script_description="Script to deploy given app")

task_deploy_app.execute(app_name=args["app"],
                        copy_only=args["copy_only"],
                        local=args["local"])
