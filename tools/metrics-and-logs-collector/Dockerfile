FROM python:3.10-slim

COPY src/metrics_and_logs_collector.py /metrics_and_logs_collector.py
COPY requirements.txt requirements.txt

RUN pip install -r requirements.txt

ENTRYPOINT ["python3", "-u", "metrics_and_logs_collector.py"]