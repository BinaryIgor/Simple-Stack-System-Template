import json
import logging
import random
import re
import signal
import sys
import time
from datetime import datetime
from os import environ

import docker
from docker.errors import NotFound

import logs_exporter
import metrics_exporter

# logging.basicConfig(level=logging.INFO,
#                     format="%(asctime)s.%(msecs)03d [%(levelname)s] %(message)s",
#                     datefmt="%Y-%m-%d %H:%M:%S")

formatter = logging.Formatter("%(asctime)s.%(msecs)03d [%(levelname)s] %(message)s", "%Y-%m-%d %H:%M:%S")
sh = logging.StreamHandler()
sh.setFormatter(formatter)

log = logging.getLogger(__file__)
log.setLevel(level=logging.INFO)
log.addHandler(sh)

CONTAINER_ID_FIELD = "containerId"
CONTAINER_NAME_FIELD = "containerName"

MACHINE_NAME = environ.get("MACHINE_NAME", "anonymous-machine")

METRICS_COLLECTION_INTERVAL = int(environ.get("METRICS_COLLECTION_INTERVAL", 10))
LOGS_COLLECTION_INTERVAL = int(environ.get("LOGS_COLLECTION_INTERVAL", 3))

LAST_METRICS_READ_AT_FILE_PATH = environ.get("LAST_METRICS_READ_AT_FILE",
                                             "/tmp/last-metrics-read-at.txt")
LAST_LOGS_READ_AT_FILE_PATH = environ.get("LAST_LOGS_READ_AT_FILE",
                                          "/tmp/last-logs-read-at.txt")

MAX_LOGS_NOT_SEND_AGO = 10 * 60

SEND_METRICS_RETRIES = 10
SEND_METRICS_RETRY_DELAY = 2

SEND_LOGS_RETRIES = 10
SEND_LOGS_RETRY_DELAY = 2

EXPORTER_PORT = int(environ.get("EXPORTER_PORT", 8080))

log.info(f"Testing log {datetime.now()}")


class DockerContainers:
    """
    Wrapper class for containers states.
    We need previous_containers field to make sure that we collect metrics & logs from containers that died after previous collection,
    but before the next one
    """

    def __init__(self, docker_client):
        self.previous_containers = []
        self.client = docker_client

    def get(self):
        def container_name(container):
            return container["Names"][0].replace("/", "")

        fetched = [{CONTAINER_ID_FIELD: c['Id'],
                    CONTAINER_NAME_FIELD: container_name(c)}
                   for c in self.client.containers()]

        all_containers = []
        all_containers.extend(fetched)

        for pc in self.previous_containers:
            if pc not in all_containers:
                all_containers.append(pc)

        self.previous_containers = fetched

        return all_containers


class GracefulShutdown:
    stop = False

    def __init__(self):
        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

    # Args are needed due to signal handler specification
    def exit_gracefully(self, *args):
        self.stop = True


shutdown = GracefulShutdown()


def connected_docker_client_retrying():
    def new_client():
        return docker.APIClient(base_url="unix://var/run/docker.sock")

    log.info(f"Starting monitoring of {MACHINE_NAME}...")

    while True:
        try:
            log.info("Trying to get client...")
            client = new_client()
            ver = data_object_formatted(client.version())
            log.info(f"Client connected, docker version: {ver}")
            return client
        except Exception:
            if shutdown.stop:
                log.info("Shutdown requested, exiting")
                sys.exit(0)

            retry_interval = random_retry_interval()
            log_exception(f"Problem while connecting to docker client, retrying in {retry_interval}s...")
            time.sleep(retry_interval)


def data_object_formatted(data_object):
    return json.dumps(data_object, indent=2)


def random_retry_interval():
    return round(random.uniform(1, 5), 3)


def log_exception(message):
    log.exception(f"{message}")
    print()


def current_timestamp():
    return int(time.time())


def current_timestamp_millis():
    return int(time.time() * 1000)


docker_containers = DockerContainers(connected_docker_client_retrying())


def keep_collecting_and_exporting():
    try:
        do_keep_collecting_and_exporting()
    except Exception:
        log_exception("Problem while collecting, retrying...")
        keep_collecting_and_exporting()


def do_keep_collecting_and_exporting():
    collection_interval = min(METRICS_COLLECTION_INTERVAL, LOGS_COLLECTION_INTERVAL)
    last_metrics_collection_timestamp = 0
    last_logs_collection_timestamp = 0

    containers_last_log_check_timestamps = {}
    default_last_logs_collection_timestamp = initial_last_logs_gathered_timestamp()

    while True:
        if shutdown.stop:
            log.info("Shutdown requested, exiting gracefully")
            break

        timestamp = current_timestamp()

        log.info("Checking containers...")
        containers = docker_containers.get()

        should_gather_metrics = timestamp - last_metrics_collection_timestamp >= METRICS_COLLECTION_INTERVAL
        should_gather_logs = timestamp - last_logs_collection_timestamp >= LOGS_COLLECTION_INTERVAL

        if should_gather_metrics:
            gather_and_export_metrics(containers)
            last_metrics_collection_timestamp = current_timestamp()
        if should_gather_logs:
            containers_last_log_check_timestamps = last_logs_checks_synced_with_running_containers(containers,
                                                                                                   containers_last_log_check_timestamps,
                                                                                                   default_last_log_check_timestamp=default_last_logs_collection_timestamp)
            default_last_logs_collection_timestamp = timestamp
            gather_and_export_logs(containers, containers_last_log_check_timestamps)
            last_logs_collection_timestamp = current_timestamp()

        print("...")

        metrics_exporter.on_next_collection(MACHINE_NAME)

        if shutdown.stop:
            log.info("Shutdown requested, exiting gracefully")
            break

        print(f"Sleeping for {collection_interval}s")
        print()

        time.sleep(collection_interval)


def gather_and_export_metrics(containers):
    log.info(f"Have {len(containers)} running containers, checking their metrics/stats...")

    last_metrics_read_at = current_timestamp()

    for c in containers:
        c_id = c[CONTAINER_ID_FIELD]
        c_name = c[CONTAINER_NAME_FIELD]

        print()
        log.info(f"Checking {c_name}:{c_id} container metrics...")

        c_metrics = fetched_container_metrics(container_id=c_id,
                                              container_name=c_name)

        metrics_exporter.on_new_container_metrics(MACHINE_NAME, c_metrics)

        log.info(f"{c_name}:{c_id} container metrics checked")

    print()
    log.info("Metrics checked.")
    print()

    update_last_data_read_at_file(LAST_METRICS_READ_AT_FILE_PATH, last_metrics_read_at)


def collect_and_export_containers_metrics(containers):
    containers_metrics = []

    for c in containers:
        c_id = c[CONTAINER_ID_FIELD]
        c_name = c[CONTAINER_NAME_FIELD]

        print()
        log.info(f"Checking {c_name}:{c_id} container metrics...")

        c_metrics = fetched_container_metrics(container_id=c_id,
                                              container_name=c_name)

        if c_metrics:
            containers_metrics.append(c_metrics)
            print()

        log.info(f"{c_name}:{c_id} container metrics checked")

    return containers_metrics


def fetched_container_metrics(container_id, container_name):
    try:
        log.info("Gathering metrics...")
        c_metrics = docker_containers.client.stats(container_id, stream=False)

        memory_metrics = c_metrics["memory_stats"]
        prev_cpu_metrics = c_metrics["precpu_stats"]
        cpu_metrics = c_metrics["cpu_stats"]

        inspection_result = docker_containers.client.inspect_container(container_id)
        started_at = inspection_result['State'].get('StartedAt', datetime.utcnow().isoformat(sep="T"))
        # make date format compatible with python iso format impl
        started_at = re.sub("(\\.[0-9]+)", "", started_at).replace("Z", "")
        started_at = int(datetime.fromisoformat(started_at).timestamp())

        container_metrics = formatted_container_metrics(name=container_name,
                                                        started_at=started_at,
                                                        memory_metrics=memory_metrics,
                                                        cpu_metrics=cpu_metrics,
                                                        precpu_metrics=prev_cpu_metrics)

        log.info("Metrics gathered")

        return container_metrics
    except NotFound:
        log.info(f"Container {container_name}:{container_id} not found, skipping!")
        print()
        return None
    except json.decoder.JSONDecodeError:
        log.info(f"Container {container_name}:{container_id} returned invalid json, skipping!")
        print()
        return None
    except Exception:
        log_exception("Failed to gather metrics")
        return None


def formatted_container_metrics(name, started_at, memory_metrics, cpu_metrics, precpu_metrics):
    try:
        cpu_usage = container_cpu_metrics(cpu_metrics, precpu_metrics)
        return metrics_exporter.ContainerMetrics(name=name,
                                                 started_at=started_at,
                                                 timestamp=current_timestamp(),
                                                 used_memory=memory_metrics["usage"],
                                                 max_memory=memory_metrics["limit"],
                                                 cpu_usage=cpu_usage)

    except KeyError:
        # We get this, when docker_client.stats() return empty metrics for killed container. We don't care about that
        return None


def container_cpu_metrics(cpu_metrics, precpu_metrics):
    prev_usage = precpu_metrics['cpu_usage']
    prev_container_usage = prev_usage['total_usage']
    prev_system_usage = precpu_metrics['system_cpu_usage']

    current_usage = cpu_metrics['cpu_usage']
    current_container_usage = current_usage['total_usage']
    current_system_usage = cpu_metrics['system_cpu_usage']

    cpu_delta = current_container_usage - prev_container_usage
    system_delta = current_system_usage - prev_system_usage

    if cpu_delta > 0 and system_delta > 0:
        percpu_usages = current_usage.get('percpu_usage')
        cores_num = len(percpu_usages) if percpu_usages else 1
        cpu_usage = (cpu_delta / system_delta) * cores_num
        # Value is in the range of 0 - 1, so multiplying it by 100 we need only up to 2 digits precision like 12.34%
        return round(cpu_usage, 4)

    return 0


def gather_and_export_logs(containers, containers_last_log_check_timestamps):
    log.info(f"Have {len(containers)} running containers, checking their logs...")

    last_logs_read_at = current_timestamp()

    for c in containers:
        app = c[CONTAINER_NAME_FIELD]

        last_logs_check_timestamp = containers_last_log_check_timestamps[app]
        c_logs = fetched_container_logs(containers_last_log_check_timestamps, last_logs_check_timestamp, app)

        if c_logs:
            container_logs = logs_exporter.ContainerLogs(app, c_logs)
            logs_exporter.export(MACHINE_NAME, container_logs)

        log.info(f"{app} logs checked")

    print()

    update_last_data_read_at_file(LAST_LOGS_READ_AT_FILE_PATH, last_logs_read_at)


def fetched_container_logs(containers_last_log_check_timestamps, last_logs_check_timestamp, container_name):
    try:
        log.info("Gathering logs...")

        now = current_timestamp()

        c_logs = docker_containers.client.logs(container_name, since=last_logs_check_timestamp, until=now,
                                               stream=False).decode("utf-8")

        containers_last_log_check_timestamps[container_name] = now

        log.info("Logs gathered")

        return c_logs if c_logs else None
    except NotFound:
        log.info(f"Container {container_name} not found, skipping!")
        print()
        return None
    except Exception:
        log_exception("Failed to gather logs")
        return None


def initial_last_logs_gathered_timestamp():
    """
    Restarting script and gathering metadata on its start takes a while, so we go back in time by arbitrary 10s.
    """
    return current_timestamp() - 10


def limited_last_logs_gathered_timestamp(last_logs_gathered_timestamp):
    max_last_logs_ago = current_timestamp() - MAX_LOGS_NOT_SEND_AGO
    return max(max_last_logs_ago, last_logs_gathered_timestamp)


def last_logs_checks_synced_with_running_containers(running_containers,
                                                    containers_last_log_check_timestamps,
                                                    default_last_log_check_timestamp):
    synced_checks = {}

    for c in running_containers:
        app = c[CONTAINER_NAME_FIELD]
        last_container_check = containers_last_log_check_timestamps.get(app, default_last_log_check_timestamp)
        synced_checks[app] = last_container_check

    return synced_checks


def update_last_data_read_at_file(file, read_at):
    try:
        log.info(f"Updating last-data-read-at file: {file}")

        with open(file, "w") as f:
            f.write(str(read_at))

        print()
    except Exception:
        log_exception("Problem while updating last data read at file...")


metrics_exporter.export(EXPORTER_PORT)
keep_collecting_and_exporting()
