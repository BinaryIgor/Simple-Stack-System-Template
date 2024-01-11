# System architecture and assumptions

* https://grafana.com/oss/loki/
* https://grafana.com/docs/loki/latest/send-data/docker-driver/
* simple, yet flexible CI - Bash & Python, where Bash is not enough
* can build apps locally, but would like to avoid storing secrets locally
  * have a script to encrypt and decrypt them
  * store them on a google drive/S3 bucket/DO space/locally, but encrypted
  * have a script to send it to all machines and decrypt them there, storing them in a unified location (same on all machines)
* scale - err towards less machines, but can be a few (3 - 10), just not dozens/thousands
* to simplify their management - have the same user, folders structure and other (what?) conventions
* file (json?) format to configure apps - similar to Helm for Kubernetes in a way
* if build on a machine (system monitor or whatever) - could upload SSH keys to Github and have pull access based on that
* /etc/hosts - add private ip addresses of other services that need to communicate with each other
* everything is on Docker with network host to allow easy machine-to-machine VPC communication
* monitoring
  * Prometheus
  * logs -> Loki/Custom solution?
  * Do we need some kind of system monitor or can we live without it?
  * 