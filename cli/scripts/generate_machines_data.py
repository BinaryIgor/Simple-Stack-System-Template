import json
from os import environ

from commons import meta, resources, machines

log = meta.new_log("generate_machines_data")

meta.cmd_args(requires_confirm=True, prod_env=True)

do_api_token = environ.get("DO_API_TOKEN")
if not do_api_token:
    do_api_token = input("DO_API_TOKEN env not set, we need it to access Digital Ocean resources: ")
resources.set_api_token(do_api_token)


def ip_addresses(networks_data):
    public_ip = None
    private_ip = None

    for address in networks_data:
        if address['type'] == 'public':
            public_ip = address['ip_address']
        if address['type'] == 'private':
            private_ip = address['ip_address']

    return public_ip, private_ip


data_path = machines.data_path()

log.info(f"Generating machines representation for scripts usage to {data_path} path...")
log.info("Getting machines..")

machines_data = []

for m in resources.get_droplets():
    public_ip, private_ip = ip_addresses(m['networks']['v4'])
    public_ip6, private_ip6 = ip_addresses(m['networks']['v6'])

    machine_data = {
        machines.NAME: m[machines.NAME],
        machines.PUBLIC_IP: public_ip,
        machines.PRIVATE_IP: private_ip
    }

    if public_ip6:
        machine_data[machines.PUBLIC_IP6] = public_ip6
    if private_ip6:
        machine_data[machines.PRIVATE_IP6] = private_ip6

    print()
    log.info(f"Machine data: {machine_data}")

    machines_data.append(machine_data)

print()
log.info(f"Have data for machines, saving it...")

with open(machines.data_path(), "w") as f:
    json.dump(machines_data, f, indent=2)

print()
log.info("Machines data saved")
