import math
import statistics
import subprocess
import time
from concurrent.futures import ThreadPoolExecutor

from commons import meta

log = meta.new_log("execute_load_test")

args = meta.cmd_args(env_arg=False,
                     args_definition={
                         "requests": {
                             "required": True,
                             "type": int
                         },
                         "rate_per_second": {
                             "required": True,
                             "type": int
                         },
                         "parallelism": {
                             "type": int
                         },
                         "curl_endpoint": {
                             "required": True
                         },
                         "curl_auth_header": {
                             "required": True
                         }
                     })

curl_endpoint = args["curl_endpoint"]
curl_auth_header = args["curl_auth_header"]

curl_cmd = f"curl -s -o /dev/null -H '{curl_auth_header}' {curl_endpoint}"

log.info("About to execute http load test with curl command:")
print(curl_cmd)


def task(sleep, idx):
    if sleep > 0:
        time.sleep(sleep)

    start = time.time()

    should_print = idx % rate_per_second == 0

    if should_print:
        log.info(f"About to execute {idx} request...")

    subprocess.run(f"""
    #!/bin/bash
    set -e
    {curl_cmd}
    """, shell=True, check=True)

    if should_print:
        log.info(f"{idx} request executed!")

    return time.time() - start


def percentile(data, percentile):
    if percentile >= 100:
        return data[-1]

    if percentile <= 1:
        return data[0]

    n = len(data)
    if n == 0:
        raise Exception("no percentile for empty data")

    index = n * percentile / 100

    if index.is_integer():
        return data[round(index)]

    lower_idx = math.floor(index)
    upper_idx = math.ceil(index)

    if lower_idx < 0:
        return data[upper_idx]

    if upper_idx >= n:
        return data[lower_idx]

    return (data[lower_idx] + data[upper_idx]) / 2


rate_per_second = args["rate_per_second"]
delay = 1.0 / rate_per_second

requests = args["requests"]
parallelism = args.get("parallelism", requests)

start = time.time()

with ThreadPoolExecutor(max_workers=parallelism) as executor:
    futures = []
    for i in range(requests):
        task_delay = i * delay
        future = executor.submit(task, task_delay, i)
        futures.append(future)

    results = [f.result() for f in futures]

duration = round(time.time() - start, 2)

print(f"{requests} requests with {rate_per_second} rate took {duration} seconds")

sorted_results = sorted(results)

mean = statistics.mean(sorted_results)
median = statistics.median(sorted_results)
std = statistics.stdev(sorted_results)
percentile_75 = percentile(sorted_results, 75)
percentile_90 = percentile(sorted_results, 90)
percentile_95 = percentile(sorted_results, 95)
percentile_99 = percentile(sorted_results, 99)
percentile_100 = percentile(sorted_results, 100)

print("Stats...")
print(f"Mean: {mean}")
print(f"Median: {median}")
print(f"Std: {std}")
print(f"Percentile 75: {percentile_75}")
print(f"Percentile 90: {percentile_90}")
print(f"Percentile 95: {percentile_95}")
print(f"Percentile 99: {percentile_99}")
print(f"Percentile 100: {percentile_100}")
