import json
from os import path

from . import meta

log = meta.new_log("infra")


def infra_dir():
    return meta.current_env_file_path("infra")


def tags():
    with open(path.join(infra_dir(), "tags.json")) as f:
        return json.load(f)


def settings():
    settings_path = path.join(infra_dir(), "settings.json")
    with open(settings_path) as f:
        return json.load(f)


def vpc():
    vpc_file = meta.file_with_replaced_placeholders(path.join(infra_dir(), "vpc.json"), settings())
    return json.loads(vpc_file)
