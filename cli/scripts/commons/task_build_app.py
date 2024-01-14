import shutil
from datetime import datetime
from os import path

from . import meta

# TODO: multi instances build and deploy
CI_REPO_ROOT_PATH = "CI_REPO_ROOT_PATH"
CI_ENV = "CI_ENV"
CI_PACKAGE_TARGET = "CI_PACKAGE_TARGET"
CI_BUILD_COMMONS = "CI_BUILD_COMMONS"
CI_SKIP_TESTS = "CI_SKIP_TESTS"

SECRETS_ENV_PREFIX = "secrets:"
FILE_ENV_PREFIX = "file:"

BUILD_ENV = "build_env"
RUN_APP_SCRIPT = "run_app.bash"
LOAD_AND_RUN_APP_SCRIPT = "load_and_run_app.bash"

log = meta.new_log("task_build_app")


def execute(app, skip_commons=False, skip_tests=False, skip_docker_image_export=False):
    log.info(f"About to read {app} app config...")

    app_config = meta.app_of_name(app)

    tag = _new_tag()

    log.info(f"Config read, package will be created with tag: {tag}")

    build_env = {}
    if skip_commons:
        build_env[CI_BUILD_COMMONS] = "false"
    if skip_tests:
        build_env[CI_SKIP_TESTS] = "true"

    log.info("About to build app...")
    _build_app(app_name=app, app_config=app_config, tag=tag, build_env=build_env)

    print()
    log.info(f"App built, packaging it...")
    _package_app(app_name=app, app_env_config=meta.app_env_config(app_config), image_tag=tag,
                 skip_image_export=skip_docker_image_export)

    print()
    log.info(f"{app} app is ready to deploy!")


def _new_tag():
    if meta.is_local_env():
        return "latest"

    return datetime.utcnow().strftime("%Y%m%d%H%M%S")


def _build_app(app_name, app_config, tag, build_env):
    app_dir = meta.app_dir(app_config)
    app_env_config = meta.app_env_config(app_config)

    log.info(f"About to build {app_name} app docker image with {tag} from {app_dir}")

    app_package_dir = meta.cli_app_package_dir(app_name)
    log.info(f"Prepare app package dir: {app_package_dir}")

    _prepare_app_package_dir(app_package_dir)

    app_build_cmd = _build_cmd(app_env_config)
    if app_build_cmd:
        log.info(f"Executing build cmd with app env: {app_build_cmd}")

        meta.execute_bash_script(f"""
        cd {app_dir}
        {_app_build_env_exports_str(app_name, app_env_config, build_env)}
        {app_build_cmd}
        """, script_name="Build app cmd")
        print()
        log.info("Build cmd executed")

    meta.execute_bash_script(f"""
    cd {app_dir}
    docker build . -t {app_name}:{tag}
    """)


def _build_cmd(app_env_config):
    return app_env_config.get("build_cmd", "")


def _pre_run_cmd(app_config):
    return app_config.get("pre_run_cmd", "")


def _post_run_cmd(app_config):
    return app_config.get("post_run_cmd", "")


def _zero_downtime_deploy_config(app_config):
    return app_config.get("zero_downtime_deploy", {})


def _app_restart_policy(app_config):
    if meta.is_local_env():
        return ""
    else:
        policy = app_config.get("restart_policy", "unless-stopped")
        return f"--restart {policy}"


def _stop_timeout(app_config):
    return app_config.get("stop_timeout", 30)


def _script_comments(app_config):
    return app_config.get("comments", [])


def _docker_run_params(app_config):
    return app_config.get("run_args", [])


def _container_extra_args(app_config):
    return app_config.get("container_extra_args", [])


def _app_build_env_exports_str(app_name, app_config, build_env):
    def export_env_var_str(key, value):
        return f'export {key}="{value}"'

    env = app_config.get(BUILD_ENV, {})
    exports = [
        export_env_var_str(CI_REPO_ROOT_PATH, meta.root_dir()),
        export_env_var_str(CI_PACKAGE_TARGET, meta.cli_app_package_dir(app_name)),
        export_env_var_str(CI_ENV, meta.current_env())
    ]

    if build_env:
        for k, v in build_env.items():
            exports.append(export_env_var_str(k, v))

    for k, v in env.items():
        value = _env_variable_value(v)
        exports.append(export_env_var_str(k, value))

    return "\n".join(exports)


def _env_variable_value(var_value):
    str_value = str(var_value)

    # Python changes booleans from json to its representation, which starts with capital letter
    if str_value == 'True' or str_value == 'False':
        return str_value.lower()

    return str_value


def _prepare_app_package_dir(app_package_dir):
    if path.exists(app_package_dir):
        shutil.rmtree(app_package_dir)
    meta.create_dir(app_package_dir)


def _package_app(app_name, app_env_config, image_tag, skip_image_export=False):
    log.info(f"About to create a package for {app_name} app...")

    tagged_image_name = f"{app_name}:{image_tag}"

    app_package_dir = meta.cli_app_package_dir(app_name)
    docker_image_tar = f"{app_name}.tar.gz"
    docker_image_path = path.join(app_package_dir, docker_image_tar)

    if skip_image_export:
        log.info("Skipping image export!")
    else:
        log.info(f"Dirs created. Exporting docker image to {docker_image_path}, this can take a while...")
        meta.execute_bash_script(f"docker save {tagged_image_name} | gzip > {docker_image_path}")

    run_app_script = _prepared_run_app_script(app_name, app_env_config, tagged_image_name)
    meta.write_to_file(path.join(app_package_dir, RUN_APP_SCRIPT), run_app_script)

    load_and_run_app_script = _prepared_load_and_run_app_script(tagged_image_name, docker_image_tar)
    meta.write_to_file(path.join(app_package_dir, LOAD_AND_RUN_APP_SCRIPT), load_and_run_app_script)


def _prepared_run_app_script(app_name, app_config, tagged_image_name):
    zero_downtime_deploy_config = _zero_downtime_deploy_config(app_config)
    run_app_script_template_path = _run_app_template(zero_downtime_deploy_config)

    restart_policy = _app_restart_policy(app_config)
    stop_timeout = _stop_timeout(app_config)

    script_comments = _script_comments(app_config)
    if script_comments:
        comment = "\n".join([f"#{c}" for c in script_comments])
    else:
        comment = ""

    run_lines = [f'docker run -d {restart_policy} \\\n']

    params = _docker_params(app_config)
    for p in params:
        run_lines.append(f'{p} \\\n')

    run_lines.append(f'--name {app_name} \\\n')
    run_lines.append(f'{tagged_image_name}')

    for rp in _docker_run_params(app_config):
        run_lines.append(f" \\\n")
        run_lines.append(rp)

    for ea in _container_extra_args(app_config):
        run_lines.append(f" \\\n")
        run_lines.append(ea)

    app_pre_run_cmd = _pre_run_cmd(app_config)
    app_run_cmd = "".join(run_lines)
    app_post_run_cmd = _post_run_cmd(app_config)

    return meta.file_with_replaced_placeholders(run_app_script_template_path,
                                                _run_app_script_placeholders_replacement(comment=comment,
                                                                                         app_name=app_name,
                                                                                         stop_timeout=stop_timeout,
                                                                                         pre_run_cmd=app_pre_run_cmd,
                                                                                         run_cmd=app_run_cmd,
                                                                                         post_run_cmd=app_post_run_cmd,
                                                                                         zero_downtime_deploy_config=zero_downtime_deploy_config))


def _prepared_load_and_run_app_script(tagged_image_name, docker_image_tar):
    return "\n".join([
        '#!/bin/bash',
        f'echo "Loading {tagged_image_name} image, this can take a while..."',
        f'docker load < {docker_image_tar}',
        'echo "Image loaded, running it..."',
        f'exec bash {RUN_APP_SCRIPT}'
    ])


def _run_app_template(zero_downtime_deploy):
    if zero_downtime_deploy:
        run_app_tmpl = "run_zero_downtime_app.bash"
    else:
        run_app_tmpl = "run_app.bash"

    return path.join(meta.cli_templates_dir(), run_app_tmpl)


def _docker_params(app_config):
    params = []

    for v in app_config.get("volumes", []):
        params.append(f'-v "{v}"')

    for p in app_config.get("ports", []):
        params.append(f'-p "{p}"')

    for k, v in app_config.get('env', {}).items():
        value = _env_variable_value(v)
        params.append(f'-e "{k}={value}"')

    memory = app_config.get("memory")
    if memory:
        params.append(f'--memory "{memory}"')

    cpus = app_config.get("cpus")
    if cpus:
        params.append(f'--cpus "{cpus}"')

    network = app_config.get("network")
    if network:
        params.append(f'--network "{network}"')

    hostname = app_config.get("hostname")
    if hostname:
        params.append(f'--hostname "{hostname}"')

    for u in app_config.get('ulimits', []):
        params.append(f'--ulimit "{u}"')

    for ea in app_config.get("docker_extra_args", []):
        params.append(ea)

    return params


def _run_app_script_placeholders_replacement(comment,
                                             app_name,
                                             stop_timeout,
                                             pre_run_cmd,
                                             run_cmd,
                                             post_run_cmd,
                                             zero_downtime_deploy_config):
    placeholders_replacement = {
        "comment": comment,
        "app": app_name,
        "stop_timeout": stop_timeout,
        "pre_run_cmd": pre_run_cmd,
        "run_cmd": run_cmd,
        "post_run_cmd": post_run_cmd
    }

    if zero_downtime_deploy_config:
        placeholders_replacement["upstream_nginx_dir"] = zero_downtime_deploy_config["upstream_nginx_dir"]
        new_app_url_file = path.join(meta.cli_app_package_dir(app_name),
                                     zero_downtime_deploy_config["app_url_file"])

        app_url = meta.file_content(new_app_url_file).strip()
        placeholders_replacement["app_url"] = app_url

        app_health_check_path = zero_downtime_deploy_config.get("app_health_check_path")
        if app_health_check_path:
            placeholders_replacement['app_health_check_url'] = f'{app_url}/${app_health_check_path}'
        else:
            placeholders_replacement['app_health_check_url'] = zero_downtime_deploy_config.get("app_health_check_url",
                                                                                               app_url)

    return placeholders_replacement
