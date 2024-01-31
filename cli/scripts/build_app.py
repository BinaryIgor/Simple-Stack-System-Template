from commons import meta, task_build_app

args = meta.cmd_args({
    "app": {
        "help": "App name",
        "required": True
    },
    "skip_commons": {
        "help": "Skip common libs build",
        "action": "store_true"
    },
    "skip_tests": {
        "help": "Skip tests, if you don't need to be safe and want to move faster",
        "action": "store_true"
    },
    "skip_docker_image_export": {
        "help": "Don't export final docker image to tar, which is time-consuming and not needed for local builds (in most cases)",
        "action": "store_true"
    },
}, script_description="Script to build single app")

task_build_app.execute(app_name=args["app"],
                       skip_commons=args["skip_commons"],
                       skip_tests=args["skip_tests"],
                       skip_docker_image_export=args["skip_docker_image_export"])
