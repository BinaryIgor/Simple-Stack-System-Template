import json
import logging
import os
import pathlib
import re
import signal
import subprocess as sp
import sys
from argparse import ArgumentParser, SUPPRESS, RawTextHelpFormatter
from os import path

CLI_NAME = "simple-stack-system-template-cli"

APPS_JSON = "apps.json"

ORDER_KEY = "order"
DEFAULT_APP_ORDER = 99

NAME = "name"
DIR = "dir"

_APPS = None

ENV = "env"
LOCAL_ENV = "local"
_ENV = None

VARS_PATTERN = re.compile("\\${([^}]*)}")
OBJECTS_PATTERN = re.compile('"ob#(.*)"')


def new_log(name=None):
    root_logger = logging.getLogger()
    root_logger.setLevel(level=logging.INFO)

    if not root_logger.handlers:
        console_formatter = logging.Formatter('%(asctime)s [%(levelname)s] %(name)s: %(message)s')
        sh = logging.StreamHandler()
        sh.setFormatter(console_formatter)
        root_logger.addHandler(sh)

    return root_logger if name is None else logging.getLogger(name)


log = new_log('meta')


def cmd_args(args_definition=None,
             env=None,
             prod_env=False,
             env_arg=True,
             script_description=None,
             requires_confirm=False):
    if args_definition is None:
        args_definition = {}

    if prod_env:
        env = "prod"

    if script_description:
        formatted_description = f"description:\n{script_description}"
    else:
        formatted_description = None

    parser = _new_args_parser(args_definition, formatted_description, env=env, env_arg=env_arg,
                              requires_confirm=requires_confirm)

    parsed_args = vars(parser.parse_args())

    global _ENV
    _ENV = parsed_args.get(ENV, env)
    if _ENV is None:
        _ENV = LOCAL_ENV

    _handle_exit_signals()

    return parsed_args


def _new_args_parser(args_definition, formatted_description, env=None, env_arg=True, requires_confirm=False):
    parser = ArgumentParser(add_help=False, formatter_class=RawTextHelpFormatter, description=formatted_description)

    required_args = parser.add_argument_group('required arguments')
    optional_args = parser.add_argument_group('optional arguments')

    optional_args.add_argument('-h', '--help',
                               action='help',
                               default=SUPPRESS,
                               help='show this help message and exit')

    if env is None and env_arg:
        required_args.add_argument(f'--{ENV}', '-e', help='environment to operate on', required=True)

    if requires_confirm:
        required_args.add_argument("--execute",
                                   help="This is potentially dangerous operation, so it is required to pass an additional flag",
                                   required=True,
                                   action="store_true")

    for a_name, a_def in args_definition.items():
        if a_def.get('required', False):
            required_args.add_argument(f'--{a_name}', **a_def)
        else:
            optional_args.add_argument(f'--{a_name}', **a_def)

    return parser


def _handle_exit_signals():
    # args argument required by the API
    def exit_gracefully(*args):
        print()
        log.info("Exit requested, stopping current script.")
        print()
        sys.exit(0)

    signal.signal(signal.SIGINT, exit_gracefully)
    signal.signal(signal.SIGTERM, exit_gracefully)


def multiline_description(*lines):
    return "\n".join(lines)


def cli_root_dir():
    start_cwd = os.getcwd()
    cwd = start_cwd
    for i in range(5):
        files = os.listdir(cwd)
        if 'cli-root' in files:
            return cwd

        cwd, _ = os.path.split(cwd)

    raise Exception(f"Root cli not found, starting from: {start_cwd}")


def root_dir():
    root, _ = path.split(cli_root_dir())
    return root


def root_src_dir():
    return path.join(root_dir(), "src")


def cli_config_dir():
    return path.join(cli_root_dir(), "config")


def cli_secrets_dir():
    return path.join(cli_config_dir(), current_env(), "secrets")


def cli_target_dir():
    return path.join(cli_root_dir(), "target")


def cli_app_package_dir(app_name):
    return path.join(cli_target_dir(), app_name)


def cli_templates_dir():
    return path.join(cli_root_dir(), "templates")


def sorted_apps(reverse=False, names=None):
    global _APPS
    if _APPS is None:
        with open(path.join(cli_config_dir(), APPS_JSON)) as f:
            _APPS = json.load(f)

    ordered = sorted(_APPS, key=lambda a: a.get(ORDER_KEY, DEFAULT_APP_ORDER), reverse=reverse)

    if names:
        ordered = [o for o in ordered if o[NAME] in names]

    return ordered


def current_env():
    return _ENV


def is_local_env():
    return current_env() == LOCAL_ENV


def app_of_name(app_name):
    for a in sorted_apps():
        if a[NAME] == app_name:
            return a

    raise Exception(f"{app_name} doesn't exist")


def env_config():
    with open(current_env_file_path('config.json')) as f:
        return json.load(f)


def current_env_file_path(file_name):
    return path.join(cli_config_dir(), current_env(), file_name)


def app_env_config(app):
    config_path = path.join(app_dir(app), "config", f"{current_env()}.json")
    json_file = file_with_replaced_placeholders(config_path, env_config())
    return json.loads(json_file)


def deploy_config():
    with open(current_env_file_path("deploy.json")) as f:
        return json.load(f)


def app_dir(app):
    return path.join(root_dir(), app[DIR])


def file_with_replaced_placeholders(file_path, vars_replacement, objects_replacement=None):
    return content_with_replaced_placeholders(content=file_content(file_path),
                                              vars_replacement=vars_replacement,
                                              objects_replacement=objects_replacement)


def file_content(file_path):
    with open(file_path) as f:
        return f.read()


def binary_file_content(file_path):
    with open(file_path, "rb") as f:
        return f.read()


def write_to_file(file_path, content):
    with open(file_path, "w") as f:
        f.write(content)


def write_binary_to_file(file_path, binary):
    with open(file_path, "wb") as f:
        f.write(binary)


def content_with_replaced_placeholders(content, vars_replacement, objects_replacement=None):
    def var_replacement(match):
        placeholder = match.group(0)
        var_name = match.group(1)
        return str(vars_replacement.get(var_name, placeholder))

    def object_replacement(match):
        placeholder = match.group(0)
        object_name = match.group(1)
        object_value = objects_replacement.get(object_name, placeholder)
        return str(object_value).replace("'", '"')

    replaced_content = re.sub(VARS_PATTERN, var_replacement, content)

    if not objects_replacement:
        return replaced_content

    return re.sub(OBJECTS_PATTERN, object_replacement, replaced_content)


def create_dir(dir_path, parents=True, exist_ok=True):
    pathlib.Path(dir_path).mkdir(parents=parents, exist_ok=exist_ok)


def execute_script(script, script_name="anonymous script", exit_on_failure=True):
    try:
        code = sp.run(script, shell=True).returncode
        if code == 0:
            return

        if exit_on_failure:
            log.error(f"Fail to execute script ({script_name}), exiting with code: {code}")
            sys.exit(code)
        else:
            raise Exception(f"Fail to execute script ({code}): {script_name}")
    except KeyboardInterrupt:
        log.info("Script execution interrupted by the user, exiting")
        sys.exit(0)


def execute_script_returning_process(script, script_name="anonymous script", exit_on_error=True):
    # stdout=sp.PIPE, stderr=sp.PIPE
    process = sp.run(script, shell=True, capture_output=True)
    if exit_on_error and process.returncode != 0:
        log.error(f"Fail to execute script: {script_name}")
        log.error(f"Exiting: {process}")
        sys.exit(process.returncode)

    return process


def execute_script_returning_process_output(script, script_name="anonymous script", exit_on_error=True):
    process = execute_script_returning_process(script, script_name, exit_on_error)
    return process.stdout.decode("utf8")


def execute_bash_script(script, script_name=None, exit_on_failure=True):
    execute_script(f"""
    #!/bin/bash
    set -e

    {script}
    """, script_name=script_name, exit_on_failure=exit_on_failure)


def str_arg_as_list(arg):
    if arg:
        return [a.strip() for a in arg.split(",")]

    return []
