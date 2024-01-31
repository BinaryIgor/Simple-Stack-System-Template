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


def create_firewalls(internal_ip_range):
    needed_firewalls_grouped_by_name = resources_grouped_by_name(infra.firewalls(internal_ip_range))

    log.info(f"Needed firewalls: {needed_firewalls_grouped_by_name.keys()}, checking their existence...")

    for f in resources.get(resources.FIREWALLS):
        f_name = f[NAME]

        needed_f = needed_firewalls_grouped_by_name.get(f_name)
        if needed_f:
            log.info(f"{f_name} firewall exists, skipping its creation")
        else:
            add_resources_to_delete(resources.FIREWALLS, f_name, f[ID])

    print()

    for f in needed_firewalls_grouped_by_name.values():
        log.info(f"Creating firewall: {f}")

        if dry_run:
            log.info("Dry run, skipping creation")
        else:
            response = resources.create(resources.FIREWALLS, f)

            f_name = f[NAME]

            if response.ok:
                log.info(f"{f_name} firewall created!")
            else:
                raise_response_exception(response, f"Fail to create {f_name} firewall")

    print()


def create_droplets():
    needed_droplets_grouped_by_name = resources_grouped_by_name(infra.droplets())

    log.info(f"Needed droplets: {needed_droplets_grouped_by_name.keys()}, checking their existence")

    droplet_ids_to_droplet_and_volume_ids = {}

    for d in resources.get(resources.DROPLETS):
        d_name = d[NAME]

        needed_d = needed_droplets_grouped_by_name.pop(d_name, None)
        if needed_d:
            log.info(f"{d_name} droplet exists, skipping its creation")
            droplet_ids_to_droplet_and_volume_ids[d_name] = {
                ID: d[ID],
                VOLUME_IDS: d[VOLUME_IDS]
            }
        else:
            add_resources_to_delete(resources.DROPLETS, d_name, d[ID])

    print()

    for d in needed_droplets_grouped_by_name.values():
        d_name = d[NAME]
        log.info(f"Creating droplet: {d_name}")

        if dry_run:
            print()
            log.info("Dry run, skipping creation")
            droplet_ids_to_droplet_and_volume_ids[d_name] = {
                ID: random_id(),
                VOLUME_IDS: []
            }
        else:
            response = resources.create(resources.DROPLETS, d)

            if response.ok:
                log.info(f"{d_name} droplet is being created!")
                created_droplet = response.json()["droplet"]
                droplet_ids_to_droplet_and_volume_ids[d_name] = {
                    ID: created_droplet[ID],
                    VOLUME_IDS: []
                }
            else:
                raise_response_exception(response, f"Fail to create {d_name} droplet")

        print()

    wait_for_droplets_creation()

    return droplet_ids_to_droplet_and_volume_ids


def resources_grouped_by_name(resources_list):
    return {res[NAME]: res for res in resources_list}


def random_id():
    return random.randrange(1, 1_000_000)


def wait_for_droplets_creation():
    print()
    log.info("Waiting for droplets creation, if needed...")
    print()

    while True:
        new_droplets = []

        for d in resources.get(resources.DROPLETS):
            d_name = d[NAME]
            d_status = d['status']

            if d_status == 'new':
                new_droplets.append(d_name)

        if new_droplets:
            log.info(f"Waiting for {new_droplets} droplets to become active...")
            print("...")
            time.sleep(10)
        else:
            log.info("All droplets are active!")
            print()
            break


def create_volumes():
    needed_volumes = infra.volumes()["volumes"]
    needed_volumes_grouped_by_name = {v["name"]: v for v in needed_volumes}

    log.info(f"Needed volumes: {needed_volumes_grouped_by_name.keys()}")
    log.info("Checking what needs to be created...")

    volume_names_to_ids = {}

    for v in resources.get(resources.VOLUMES):
        v_name = v['name']

        needed_v = needed_volumes_grouped_by_name.pop(v_name)
        if needed_v:
            log.info(f"{v_name} volume exists, skipping its creation")
            volume_names_to_ids[v_name] = v['id']
        else:
            log.info(f"{v_name} volume should be deleted")
            resources_to_delete[resources.VOLUMES].append({
                "name": v_name,
                "id": v['id']
            })

    for v in needed_volumes_grouped_by_name.values():
        v_name = v['name']
        log.info(f"Creating volume: {v_name}")

        if dry_run:
            print()
            log.info("Dry run, skipping creation")
            volume_names_to_ids[v_name] = random_id()
        else:
            response = resources.create("volumes", v)

            if response.ok:
                log.info(f"{v_name} volume is being created!")
                v_id = response.json()['volume']['id']
                volume_names_to_ids[v_name] = v_id
            else:
                raise_response_exception(response, f"Fail to create {v_name} volume")

        print()

    return volume_names_to_ids


def attach_volumes(droplet_ids_to_droplet_and_volume_ids, volume_names_to_ids):
    attachments = infra.volumes()['attachments']
    volume_ids_to_names = {v: k for k, v in volume_names_to_ids.items()}

    log.info("Checking which volumes need to be attached to droplets...")
    log.info(f"Desired state: {attachments}")
    print()

    for d in droplet_ids_to_droplet_and_volume_ids:
        d_data = droplet_ids_to_droplet_and_volume_ids[d]

        desired_droplet_volume = attachments.get(d)
        if desired_droplet_volume:
            attach_data = {
                NAME: desired_droplet_volume,
                ID: d_data[ID]
            }
        else:
            attach_data = None

        d_volume_names = [volume_ids_to_names[v] for v in d_data[VOLUME_IDS]]
        attach = check_droplet_attached_volumes(d, d_data, desired_droplet_volume, d_volume_names)

        if not attach or not attach_data:
            log.info(f"{d} droplet doesn't need any volumes")
            print()
            continue

        attach_volume_to_droplet(attach_data[NAME], d, d_data[ID])

        print()


def check_droplet_attached_volumes(droplet, droplet_data, desired_droplet_volume, droplet_volume_names):
    attach = True

    for vn in droplet_volume_names:
        if desired_droplet_volume and desired_droplet_volume == vn:
            attach = False
            log.info(f"Volume already attached to {droplet} droplet, skipping")
        else:
            log.info(f"{resources.VOLUMES_ATTACHMENTS}: {vn} should be detached from {droplet} droplet")
            resources_to_delete[resources.VOLUMES_ATTACHMENTS].append({
                "volume_name": vn,
                "droplet_name": droplet,
                "droplet_id": droplet_data[ID]
            })

    return attach


def attach_volume_to_droplet(v_name, d, d_id):
    log.info(f"About to attach {v_name} volume to {d} droplet...")
    if dry_run:
        log.info(f"Dry run, skipping")
    else:
        response = resources.create(f"{resources.VOLUMES}/actions", {
            "type": "attach",
            "volume_name": v_name,
            "droplet_id": d_id
        })

        if response.ok:
            log.info(f"{v_name} volume is being attached to {d} droplet!")
        else:
            raise_response_exception(response, f"Fail to attach {v_name} volume to {d} droplet")


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

log.info("VPC ready, creating firewalls with tags...")
create_firewalls(internal_ip_range)
print("...")
time.sleep(3)

print()
log.info("Firewalls ready, creating droplets...")
print()

droplet_ids_to_volume_ids = create_droplets()
print("...")
time.sleep(3)

print()
log.info("Droplets are ready, but remember to initialize them, if created anew!")
print()

log.info("Creating volumes..")
volume_names_to_ids = create_volumes()
print("...")
time.sleep(3)

print()
log.info("Volumes are ready, attaching them if needed...")
print()

attach_volumes(droplet_ids_to_volume_ids,  volume_names_to_ids)
print("...")

print()
log.info("Volumes are attached!")
print()

log.info("Infrastructure is ready")

log.info("Resources that are no longer used and should be deleted...")
print()

for r_type in resources_to_delete:
    print(f"{r_type}:")
    for r in resources_to_delete[r_type]:
        print(r)

    print()
