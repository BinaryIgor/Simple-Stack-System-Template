# Prometheus

Deleting no longer needed/problematic data:
1. Run prometheus with `--web.enable-admin-api` flag
2. Run: `curl -g -XPOST 'http://localhost:9090/api/v1/admin/tsdb/delete_series?match[]=ALERTS' -v`
3. To delete `{application="some-postgres", instance="localhost:9999", job="monitor", machine="monitor"}` for example, run: `curl -g -XPOST 'http://localhost:9090/api/v1/admin/tsdb/delete_series?match[]={application="some-postgres"}' -v`

To get all available metrics call: `http://localhost:9090/api/v1/label/__name__/values`