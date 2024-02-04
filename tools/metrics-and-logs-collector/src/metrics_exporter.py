import time

from prometheus_client import start_http_server, Gauge, Counter


class ContainerMetrics:

    def __init__(self, name, started_at, timestamp, used_memory, max_memory, cpu_usage):
        self.name = name
        self.started_at = started_at
        self.timestamp = timestamp
        self.used_memory = used_memory
        self.max_memory = max_memory
        self.cpu_usage = cpu_usage


# TODO: additional labels

MACHINE_LABEL = "machine"
NAME_LABEL = "name"

COMMONS_LABELS = [MACHINE_LABEL, NAME_LABEL]

collector_up_timestamp_gauge = Gauge("collector_up_timestamp_seconds", "TODO", [MACHINE_LABEL])

container_started_at_timestamp_gauge = Gauge("container_started_at_timestamp_seconds", "TODO", COMMONS_LABELS)
container_up_timestamp_gauge = Gauge("container_up_timestamp_seconds", "TODO", COMMONS_LABELS)
container_used_memory_gauge = Gauge("container_used_memory_bytes", "TODO", COMMONS_LABELS)
container_max_memory_gauge = Gauge("container_max_memory_bytes", "TODO", COMMONS_LABELS)
container_cpu_usage_gauge = Gauge("container_cpu_usage_percent", "TODO", COMMONS_LABELS)

container_logs_total = Counter("container_logs_total", 'TODO', COMMONS_LABELS + ["level"])


def export(port):
    start_http_server(port)


def on_next_collection(machine: str):
    collector_up_timestamp_gauge.labels(machine=machine).set(int(time.time()))


def on_new_container_metrics(machine: str, metrics: ContainerMetrics):
    container_started_at_timestamp_gauge.labels(machine=machine, name=metrics.name).set(metrics.started_at)

    now_seconds = int(time.time())
    container_up_timestamp_gauge.labels(machine=machine, name=metrics.name).set(now_seconds)

    container_used_memory_gauge.labels(machine=machine, name=metrics.name).set(metrics.used_memory)
    container_max_memory_gauge.labels(machine=machine, name=metrics.name).set(metrics.max_memory)

    container_cpu_usage_gauge.labels(machine=machine, name=metrics.name).set(metrics.cpu_usage)


def on_new_container_logs(machine, container, level):
    container_logs_total.labels(machine=machine, name=container, level=level).inc()
