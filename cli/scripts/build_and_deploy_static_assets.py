from commons import meta
from os import path

args = meta.cmd_args(script_description="Script to build and deploy static assets")

log = meta.new_log("build_and_deploy_static_assets")

main_app_dir = meta.app_dir(meta.app_of_name("main"))

meta.execute_bash_script(f"""
    cd {main_app_dir}
    bash build_static.bash
""")

if meta.is_local_env():
    log.info("Local env, deploy is just about copying files!")
    target = path.join(meta.cli_target_dir(), "main", "static")
    meta.execute_bash_script(f"""
        rm -r {target}/*
        cp -r {path.join(main_app_dir, "target", "static/.")} {target}/
    """)
else:
    log.info("Not implemented yet remote deploy")