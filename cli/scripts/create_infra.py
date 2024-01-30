import random
import time
from os import environ

from commons import meta, infra, resources

ID = "id"
NAME = "name"
VOLUME_IDS = "volume_ids"

log = meta.new_log("create_infra")

args = meta.cmd_args(
    args_definition={
        "dry_run": {
            "help": "Do not create resources, just log what is needed",
            "action": "store_true"
        }
    },
    prod_env=True,
    requires_confirm=True,
    script_description="""
    Script to create whole infrastructure (idempotent). Should be called when initializing/changing it
    """)

dry_run = args["dry_run"]

resources_to_delete = {
    resources.TAGS: [],
    resources.VPCS: [],
    resources.FIREWALLS: [],
    resources.DROPLETS: [],
    resources.VOLUMES: [],
    resources.VOLUMES_ATTACHMENTS: []
}


def create_tags():
    needed_tags = infra.tags()

    log.info(f"Needed tags: {needed_tags}, checking their existence...")

    for t in resources.get(resources.TAGS):
        t_name = t[NAME]

        if t_name in needed_tags:
            log.info(f"{t_name} tag exists, skipping its creation")
            needed_tags.remove(t_name)
        else:
            add_resources_to_delete(resources.TAGS, t_name)

    print()

    if len(needed_tags) == 0:
        log.info("No tags to create!")

    log.info(f"About to create tags: {needed_tags}")

    if dry_run:
        log.info("Dry run, skipping creation")
    else:
        for t in needed_tags:
            response = resources.create(resources.TAGS, {NAME: t})

            if response.ok:
                log.info(f"{t} tag created!")
            else:
                raise_response_exception(response, "Fail to create tag")

    print()


def add_resources_to_delete(resource, r_name, r_id=None):
    log.info(f"{resource}: {r_name} should be deleted")

    if r_id:
        resources_to_delete[resource].append({
            NAME: r_name,
            ID: r_id
        })
    else:
        resources_to_delete[resource].append(r_name)


def raise_response_exception(response, message):
    raise Exception(f'''{message}.
     Code: {response.status_code}
     Body: {response.content}''')


def create_vpc():
    needed_vpc = infra.vpc()
    needed_vpc_name = needed_vpc[NAME]

    log.info(f"Needed vpc: {needed_vpc_name}, checking if exists")

    ip_range = None

    for vpc in resources.get(resources.VPCS):
        v_name = vpc[NAME]

        if v_name == needed_vpc_name:
            log.info("VPC exists, no need to create it")
            ip_range = vpc['ip_range']
        else:
            add_resources_to_delete(resources.VPCS, v_name, vpc[ID])

    if ip_range:
        return ip_range

    log.info(f"Creating {needed_vpc_name} VPC...")

    if dry_run:
        ip_range = "10.dry.range.0/20"
        log.info(f"VPC needs to created, returning {ip_range} instead!")
        return ip_range

    response = resources.create(resources.VPCS, needed_vpc)

    if response.ok:
        response_data = response.json()
        log.info(f"Vpc created, response: {response_data}")

        return response_data['vpc']['ip_range']

    raise_response_exception(response, "Fail to create vpc")


do_api_token = environ.get("DO_API_TOKEN")
if not do_api_token:
    do_api_token = input("DO_API_TOKEN env not set, we need it to access Digital Ocean resources: ")
resources.set_api_token(do_api_token)

log.info("Creating infrastructure, in a idempotent way...")

if dry_run:
    print()
    log.info("Dry run set to true, only showing what is needed without creating it...")
    print()

log.info("Creating tags, to group resources...")

create_tags()
print("...")
time.sleep(3)

print()
log.info("Tags are ready, creating VPC...")
print()

internal_ip_range = create_vpc()
print("...")
time.sleep(3)

print()
log.info(f"VPC ip range {internal_ip_range}")
print()