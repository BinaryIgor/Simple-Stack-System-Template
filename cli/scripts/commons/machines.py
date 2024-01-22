import json
from os import path

from . import meta

log = meta.new_log("machines")

NAME = "name"
PUBLIC_IP = "public_ip"
PRIVATE_IP = 'private_ip'
PUBLIC_IP6 = "public_ip6"
PRIVATE_IP6 = 'private_ip6'


def data_path():
    return meta.current_env_file_path("machines.json")


def data(to_filter_names=None):
    d_path = data_path()
    if not path.exists(d_path):
        raise Exception(f"""There is no machines data under {d_path} path.
             You need to generate and commit it, by using script""")

    with open(d_path) as f:
        machines = json.load(f)

        if to_filter_names:
            return [m for m in machines if m['name'] in to_filter_names]

        return machines


def data_of_machine(to_filter_name):
    machines = data(to_filter_names=[to_filter_name])
    if not machines:
        raise Exception(f"Can't find machine of {to_filter_name} name")
    return machines[0]


def public_ips(machines):
    return {m[NAME]: m[PUBLIC_IP] for m in machines}


def private_ips(machines):
    return {m[NAME]: m[PRIVATE_IP] for m in machines}
