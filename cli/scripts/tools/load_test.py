import math
import statistics
import time
from concurrent.futures import ThreadPoolExecutor

import subprocess

CURL_ARGS = "https://binaryigor.com"

def task(sleep):
    if sleep > 0:
        time.sleep(sleep)

    start = time.time()

    subprocess.run(f"""
    #!/bin/bash
    set -e
    curl -s -o /dev/null {CURL_ARGS}
    """, shell=True, check=True)

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


rate_per_second = 10
delay = 1.0 / rate_per_second

tasks = 100
parallelism = tasks

start = time.time()

with ThreadPoolExecutor(max_workers=parallelism) as executor:
    futures = []
    for i in range(tasks):
        task_delay = i * delay
        future = executor.submit(task, task_delay)
        futures.append(future)

    results = [f.result() for f in futures]

duration = round(time.time() - start, 2)

print(f"{tasks} tasks with {rate_per_second} rate took {duration}")

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
