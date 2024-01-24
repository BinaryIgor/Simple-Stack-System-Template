import json
from os import path

from . import meta


def infra_dir():
    return meta.current_env_file_path("infra")


def tags():
    with open(path.join(infra_dir(), "tags.json")) as f:
        return json.load(f)
