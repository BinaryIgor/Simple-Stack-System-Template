import json
import logging
import pathlib
from logging.handlers import RotatingFileHandler
from os import environ, path

import metrics_exporter


class ContainerLogs:

    def __init__(self, container, logs):
        self.container = container
        self.logs = logs


LOGS_DIR = environ.get("LOGS_DIR", path.join("/tmp", "logs"))
pathlib.Path(LOGS_DIR).mkdir(exist_ok=True)

containers_loggers = {}

LOGS_CONFIG_PATH = environ.get("LOGS_CONFIG_PATH", "logs_config.json")

with open(LOGS_CONFIG_PATH) as f:
    logs_config = json.load(f)

INFO = "info"
WARNING = "warning"
ERROR = "error"


def export(machine, container_logs):
    _log_to_file(container_logs)
    _export_metrics(machine, container_logs)


def _log_to_file(container_logs):
    logger = containers_loggers.get(container_logs.container)

    if not logger:
        # TODO: more configurable
        container_logs_dir = path.join(LOGS_DIR, container_logs.container)

        pathlib.Path(container_logs_dir).mkdir(exist_ok=True)
        log_file = path.join(container_logs_dir, f"{container_logs.container}.log")

        handler = RotatingFileHandler(log_file, mode='a',
                                      maxBytes=10 * 1024 * 1024,
                                      backupCount=50)

        logger = logging.getLogger(container_logs.container)
        logger.setLevel(level=logging.INFO)
        logger.addHandler(handler)

        containers_loggers[container_logs.container] = logger

    logger.info(container_logs.logs)


def _export_metrics(machine, container_logs):
    levels_mapping = _logs_levels_mapping(container_logs.container)
    logs_level = _logs_level(levels_mapping, container_logs.logs)
    metrics_exporter.on_new_container_logs(machine, container_logs.container, logs_level)


def _logs_levels_mapping(container):
    levels_mapping = logs_config["default_levels_mapping"]

    for lm in logs_config.get("levels_mapping", []):
        container_mapping = False

        for c_keyword in lm["container_keywords"]:
            if c_keyword in container:
                container_mapping = True
                break

        if container_mapping:
            levels_mapping = lm
            break

    return levels_mapping


def _logs_level(levels_mapping, logs):
    def should_ignore_error():
        for m in messages_to_ignore_errors:
            if m in logs:
                return True

        return False

    messages_to_ignore_errors = levels_mapping.get("messages_to_ignore_errors", [])
    for ek in levels_mapping["error_keywords"]:
        if ek in logs:
            return INFO if should_ignore_error() else ERROR

    for wk in levels_mapping['warning_keywords']:
        if wk in logs:
            return WARNING

    return INFO
