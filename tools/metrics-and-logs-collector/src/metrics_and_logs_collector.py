import json
import logging
import random
import signal
import sys
import time
from datetime import datetime
from os import environ

import docker

logging.basicConfig(level=logging.INFO,
                    format="%(asctime)s.%(msecs)03d [%(levelname)s] %(message)s",
                    datefmt="%Y-%m-%d %H:%M:%S")

log = logging.getLogger(__file__)

CONTAINER_ID_FIELD = "containerId"
CONTAINER_NAME_FIELD = "containerName"

MACHINE_NAME = environ.get("MACHINE_NAME", "anonymous-machine")

CONSOLE_METRICS_TARGET = "CONSOLE_METRICS_TARGET"
CONSOLE_LOGS_TARGET = "CONSOLE_LOGS_TARGET"
METRICS_TARGET_URL = environ.get("METRICS_TARGET_URL", CONSOLE_METRICS_TARGET)
LOGS_TARGET_URL = environ.get("LOGS_TARGET_URL", CONSOLE_LOGS_TARGET)

METRICS_COLLECTION_INTERVAL = int(environ.get("COLLECTION_INTERVAL", 10))
LOGS_COLLECTION_INTERVAL = int(environ.get("COLLECTION_INTERVAL", 3))

LAST_METRICS_READ_AT_FILE_PATH = environ.get("LAST_METRICS_READ_AT_FILE",
                                             "/tmp/metrics-and-logs-collector-last-metrics-read-at.txt")
LAST_LOGS_READ_AT_FILE_PATH = environ.get("LAST_LOGS_READ_AT_FILE",
                                          "/tmp/metrics-and-logs-collector-last-logs-read-at.txt")

SEND_METRICS_RETRIES = 10
SEND_METRICS_RETRY_DELAY = 2

SEND_LOGS_RETRIES = 10
SEND_LOGS_RETRY_DELAY = 2

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


docker_containers = DockerContainers(connected_docker_client_retrying())


def keep_collecting_and_sending():
    try:
        do_keep_collecting_and_sending()
    except Exception:
        log_exception("Problem while collecting, retrying...")
        keep_collecting_and_sending()


def do_keep_collecting_and_sending():
    collection_interval = min(METRICS_COLLECTION_INTERVAL, LOGS_COLLECTION_INTERVAL)

    while True:
        if shutdown.stop:
            log.info("Shutdown requested, exiting gracefully")
            break

        log.info("Checking containers...")
        print("...")

        if shutdown.stop:
            log.info("Shutdown requested, exiting gracefully")
            break

        print(f"Sleeping for {collection_interval}s")
        print()

        time.sleep(collection_interval)


keep_collecting_and_sending()