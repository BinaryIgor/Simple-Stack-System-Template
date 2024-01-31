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


def firewalls(internal_ip_range):
    firewalls_file = meta.file_with_replaced_placeholders(path.join(infra_dir(), "firewalls.json"), {
        "internal_ip_range": internal_ip_range
    }, objects_replacement=settings())
    return json.loads(firewalls_file)


def droplets():
    setts = settings()

    droplet_init = meta.file_with_replaced_placeholders(path.join(infra_dir(), "droplet_init.bash"),
                                                        vars_replacement={
                                                            "USERNAME": setts["droplets_user"],
                                                            "INSTALL_PROMETHEUS_NODE_EXPORTER": "true"
                                                        })

    # escapes " in json, but also puts double unnecessary " at the start and end; we need to get rid of that
    setts["droplet_init.bash"] = json.dumps(droplet_init).strip()[1:-1]

    droplets_file = meta.file_with_replaced_placeholders(path.join(infra_dir(), "droplets.json"),
                                                         vars_replacement=setts,
                                                         objects_replacement=setts)

    return json.loads(droplets_file)


def volumes():
    volumes_file = meta.file_with_replaced_placeholders(path.join(infra_dir(), "volumes.json"), settings())
    return json.loads(volumes_file)


def domains():
    with open(path.join(infra_dir(), "domains.json")) as f:
        return json.load(f)
