import sys
import time
from os import path
import tempfile

from . import meta, machines, crypto

CI_MACHINE_NAME = "CI_MACHINE_NAME"
CI_DEPLOY_DIR = "CI_DEPLOY_DIR"

CI_LOCAL_SECRETS_PATH = "CI_LOCAL_SECRETS_PATH"

DOCKER_FAILED_STATUS = 'failed'
DOCKER_RUNNING_STATUS = 'running'

DEPLOY_ENV_FILE = "deploy.env"

LOAD_AND_RUN_APP_SCRIPT = "load_and_run_app.bash"

log = meta.new_log("deploy_app")

LOCAL_DEPLOY_DIR = path.join(tempfile.gettempdir(), "local-deploy")
LOCAL_SECRETS_PATH = path.join(LOCAL_DEPLOY_DIR, ".secrets")


def execute(app_name, deployment_machines=None, copy_only=False, local=False):
    log.info(f"About to deploy {app_name}, reading its config...")
    app = meta.app_of_name(app_name)
    app_config = meta.app_env_config(app)

    print()
    log.info(f"App config:")
    print(app_config)
    print()

    log.info(f"About to deploy {app_name} app, checking where exactly..")

    deploy_config = meta.deploy_config()

    app_hosts = _app_placements(app_name, deploy_config, to_filter_machines=deployment_machines)

    successful_deployments = []
    failed_deployments = []

    for ah, ip in app_hosts.items():
        print(f"{ah} - {ip}")
        try:
            print()
            if local:
                log.info(f"Executing local deployment to {LOCAL_DEPLOY_DIR} dir...")
            else:
                log.info(f"Deploying {app_name} to {ah}:{ip} machine")

            deploy_user = deploy_config["user"]
            deploy_dir = _app_package_dir(app_name, deploy_config, local=local)

            remote_host = f"{deploy_user}@{ip}"

            previous_deploy_dir = f"{deploy_dir}/previous"
            latest_deploy_dir = f"{deploy_dir}/latest"

            package_dir = meta.cli_app_package_dir(app_name)

            log.info(f"{remote_host}, {package_dir}")

            _copy_app_secrets_if(app, remote_host, deploy_config, local=local)
            _copy_app_package(machine_name=ah,
                              remote_host=remote_host,
                              previous_deploy_dir=previous_deploy_dir,
                              latest_deploy_dir=latest_deploy_dir,
                              package_dir=package_dir,
                              local=local)

            if copy_only:
                print("...")
                log.info("Copy only deploy, skipping app run!")
            else:
                log.info("About to start an app...")

                _perform_pre_deploy_actions(app_config=app_config,
                                            deploy_dir=latest_deploy_dir,
                                            machine_name=ah,
                                            remote_host=remote_host,
                                            local=local)

                _deploy_app(remote_host, latest_deploy_dir, local=local)

                _check_app_status(app_name=app_name, remote_host=remote_host, local=local)
        except KeyboardInterrupt:
            log.info("Deployment interrupted on a request!")
        except Exception:
            log.exception(f"Failed to deploy to {ah}")
            failed_deployments.append(ah)

    print()
    log.info(
        f"{app_name} is deployed on {len(successful_deployments)} hosts, but check logs to make sure that it runs as desired")

    if failed_deployments:
        print()
        log.error(f"Failed to deploy app to {failed_deployments} hosts")
        sys.exit(1)


def _app_placements(app_name, deploy_config, to_filter_machines):
    deploy_machines = deploy_config['machines']
    if to_filter_machines:
        deploy_machines_names = [d['name'] for d in deploy_machines]
        deploy_app_machines = [m for m in deploy_machines_names if m in to_filter_machines]
        if not deploy_app_machines:
            raise Exception(f"There are no machines for this app matching {deploy_app_machines} filter")
    else:
        deploy_app_machines = _app_machines(app_name, deploy_machines)

    print()
    log.info(f"{app_name} will be deployed to {deploy_app_machines} machines")

    # TODO: maybe constants?
    return {m['name']: m['public_ip'] for m in machines.data(to_filter_names=deploy_app_machines)}


def _app_machines(app_name, deploy_machines):
    app_m = []
    for m in deploy_machines:
        if app_name in m['apps']:
            app_m.append(m['name'])

    if not app_m:
        raise Exception(f"Can't find any machine for {app_name} app")

    return app_m


def _app_package_dir(app_name, deploy_config, local):
    return path.join(LOCAL_DEPLOY_DIR, app_name) if local else f"{deploy_config['deploy-dir']}/{app_name}"


def _copy_app_secrets_if(app, remote_host, deploy_config, local):
    log.info("Check if app needs secrets")
    print()

    secret_names = meta.app_env_config(app).get("secrets", [])

    if secret_names:
        if local:
            secrets_path = LOCAL_SECRETS_PATH
        else:
            secrets_path = deploy_config['secrets-path']

        secret_groups = []
        for sn in secret_names:
            s_group = crypto.secret_group(sn)
            if not s_group:
                raise Exception(f"{sn} doesn't belong to any known secret group")

            if s_group not in secret_groups:
                secret_groups.append(s_group)

        log.info(f"App needs {secret_names} secrets from {secret_groups} groups...")

        decrypted_secrets = _decrypted_secrets(secret_names, secret_groups)

        if local:
            meta.create_dir(secrets_path)
        else:
            meta.execute_bash_script(f'ssh {remote_host} "mkdir -p {secrets_path}"', exit_on_failure=False)

        log.info(f"Secrets decrypted, copying them to {secrets_path}")

        for key, secret in decrypted_secrets.items():
            log.info(f"Copying {key}...")
            secret_file = f"{key}.txt"
            tmp_secret_path = path.join(tempfile.gettempdir(), secret_file)
            meta.write_to_file(tmp_secret_path, secret)

            if local:
                copy_secret_cmd = f"cp {tmp_secret_path} {path.join(secrets_path, secret_file)}"
            else:
                copy_secret_cmd = f"scp {tmp_secret_path} {remote_host}:{secrets_path}/{secret_file}"
            meta.execute_bash_script(f"""
                {copy_secret_cmd}
                rm {tmp_secret_path}
            """, exit_on_failure=False)
    else:
        log.info("App doesn't need any secrets, skipping them")

    print()


def _decrypted_secrets(secret_names, secret_groups):
    secret_groups_encryption_passwords = {}

    for sg in secret_groups:
        sg_password = input(f"{sg} group password: ")
        secret_groups_encryption_passwords[sg] = sg_password

    decrypted_secrets = {}
    for sn in secret_names:
        group = crypto.secret_group(sn)
        enc_password = secret_groups_encryption_passwords[group]
        decrypted_secret = crypto.decrypted_secret(sn, group, enc_password)
        decrypted_secrets[sn] = decrypted_secret

    return decrypted_secrets


def _copy_app_package(machine_name,
                      remote_host,
                      previous_deploy_dir,
                      latest_deploy_dir,
                      package_dir,
                      local):
    log.info(f"Copying app package from {package_dir} to {latest_deploy_dir}, this could take a while...")

    local_deploy_env_file_path = path.join(meta.cli_target_dir(), DEPLOY_ENV_FILE)
    remote_deploy_env_file_path = path.join(latest_deploy_dir, DEPLOY_ENV_FILE)

    if local:
        export_local_env = f'export {CI_LOCAL_SECRETS_PATH}="{LOCAL_SECRETS_PATH}"'
    else:
        export_local_env = ""

    meta.write_to_file(local_deploy_env_file_path, f"""
    export {CI_MACHINE_NAME}="{machine_name}"
    export {CI_DEPLOY_DIR}="{latest_deploy_dir}"
    {export_local_env}
    """.strip())

    copy_commands = f"""
    rm -f -r {previous_deploy_dir}
    mkdir -p {latest_deploy_dir}
    cp -r {latest_deploy_dir} {previous_deploy_dir}
    rm -r {latest_deploy_dir}
    mkdir {latest_deploy_dir}
    """

    if local:
        meta.execute_bash_script(f"""
        {copy_commands}
        cp {local_deploy_env_file_path} {remote_deploy_env_file_path}
        cp -r {package_dir}/* {latest_deploy_dir}
        """, exit_on_failure=False)
    else:
        meta.execute_bash_script(f"""
        ssh {remote_host} bash -c "'{copy_commands}'"
        scp {local_deploy_env_file_path} {remote_host}:{remote_deploy_env_file_path}
        scp -r {package_dir}/* {remote_host}:{latest_deploy_dir}
        """, exit_on_failure=False)


def _perform_pre_deploy_actions(app_config, machine_name, remote_host, deploy_dir, local):
    actions = app_config.get("pre_deploy_actions")
    if not actions:
        return

    print()
    skip_actions = app_config.get("pre_deploy_actions_skip_locally", True)
    if skip_actions:
        log.info("Skipping pre deploy actions locally...")
    else:
        log.info("Performing pre deploy actions")
        _perform_deploy_actions(actions, machine_name, remote_host, deploy_dir, local=local)


def _perform_deploy_actions(actions, machine_name, remote_host, deploy_dir, local):
    for a in actions:
        cmd = meta.content_with_replaced_placeholders(a, {
            CI_MACHINE_NAME: machine_name,
            CI_DEPLOY_DIR: deploy_dir
        })
        log.info(f"Executing: {cmd}")
        if local:
            meta.execute_bash_script(cmd)
        else:
            meta.execute_bash_script(f'ssh {remote_host} "{cmd}"')


def _deploy_app(remote_host, latest_deploy_dir, local):
    deploy_app_cmd = f"cd {latest_deploy_dir}; bash {LOAD_AND_RUN_APP_SCRIPT}"
    if not local:
        deploy_app_cmd = f'ssh {remote_host} "{deploy_app_cmd}"'

    meta.execute_bash_script(deploy_app_cmd)


def _check_app_status(app_name, remote_host, local, multiple_checks=True):
    try:
        log.info("App is deployed, checking its state for a few seconds")

        checks = 3 if multiple_checks else 1
        elapsed_time = 0

        for i in range(checks):
            time.sleep(5)

            elapsed_time += 5

            check_status_cmd = "docker container inspect -f '{{.State.Status}}' " + app_name

            if not local:
                check_status_cmd = f'ssh {remote_host} "{check_status_cmd}"'

            status = meta.execute_script_returning_process_output(check_status_cmd)

            log.info(f"Check {i + 1}/{checks} after {elapsed_time} seconds from start, status: {status}")
            print()

            check_logs_cmd = f"docker logs {app_name} --since 90s"
            if not local:
                check_logs_cmd = f'ssh {remote_host} {check_logs_cmd}'

            log.info("Last log:")
            meta.execute_bash_script(check_logs_cmd)
            print()
    except KeyboardInterrupt:
        log.info("Checking status interrupted on request!")
