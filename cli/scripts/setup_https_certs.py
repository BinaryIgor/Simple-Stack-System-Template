from commons import meta, task_build_app, task_deploy_app, infra

"""
Webroot mode: certbot will create challenge file in the specified webroot to verify domain.
Make sure that given webroot-path is exposed via your http server!

https://eff-certbot.readthedocs.io/en/stable/using.html#webroot
The webroot plugin works by creating a temporary file for each of your requested domains in: 
${webroot-path}/.well-known/acme-challenge.
Then the Let’s Encrypt validation server makes HTTP requests to validate that the DNS for each requested domain
resolves to the server running certbot. An example request made to your web server would look like:
66.133.109.36 - - [05/Jan/2016:20:11:24 -0500] "GET /.well-known/acme-challenge/HGr8U1IeTW4kY_Z6UIyaakzOkyQgPr_7ArlLgtZE8SX HTTP/1.1" 200 87 "-" "Mozilla/5.0 (compatible; Let's Encrypt validation server; +https://www.letsencrypt.org)"

You can check renewal by running: sudo certbot renew --dry-run.
For some reasons, it hangs in below script.

Remember to first deploy nginx with http only (some kind of meta-deploy)!
"""

DOMAINS_EMAIL = "igor.thebinary@gmail.com"
SET_HTTPS_SERVER = "nginx-set-https-server"

log = meta.new_log("setup_https_certs")

args = meta.cmd_args(
    prod_env=True,
    requires_confirm=True,
    args_definition={
        "domains": {
            "help": meta.multiline_description("comma separated list of domains to create.",
                                               "By default, all domains defined in domains.json file will be set up")
        },
        "webroot_path": {
            "help": "Path to webroot, where certbot will be able to place challenge files"
        },
        "skip_https_server_setup": {
            "help": "Skipping building and deploy meta https server",
            "action": "store_true"
        },
        "dry_run": {
            "help": "Only print changes, do not execute. Useful for debugging",
            "action": "store_true"
        }
    },
    script_description="""Script to setup https certs for a given domain"""
)

deploy_config = meta.deploy_config()
deploy_user = deploy_config['user']
skip_https_server_setup = args['skip_https_server_setup']
dry_run = args['dry_run']

machine_domains = infra.domains()

domains = meta.str_arg_as_list(args["domains"])
if domains:
    machine_domains = [m for m in machine_domains if m['domain'] in domains]

webroot_path = args["webroot_path"]
if not webroot_path:
    webroot_path = meta.env_config()["static-path"]

log.info(f"Setting up certbot and https certs for:")
print(machine_domains)

print()
if skip_https_server_setup:
    log.info("Skipping https server setup!")
else:
    log.info("Building set-https-server first...")
    task_build_app.execute(app_name=SET_HTTPS_SERVER)

for md in machine_domains:
    machine = md['machine']
    domain = md['domain']

    log.info(f"Setting up certbot and http cert..")
    log.info(f"Machine: {machine}, domain: {domain}, and a webroot: {webroot_path}")

    remote_host = f"{deploy_user}@{domain}"

    if dry_run:
        log.info(f"Dry run, only printing changes to {remote_host}!")
    else:
        print()
        log.info(f"Deploying {SET_HTTPS_SERVER} first to pass certbot http challenge...")

        if not skip_https_server_setup:
            task_deploy_app.execute(app_name=SET_HTTPS_SERVER, deployment_machines=[machine])

        meta.execute_bash_script(f"""
        ssh {remote_host} "
        echo "Setting up certbot..."
        sudo snap install --classic certbot
        sudo ln -s /snap/bin/certbot /usr/bin/certbot
        echo
        echo "Certbot configured, generating certs..."
        sudo certbot certonly --webroot --webroot-path "{webroot_path}" --domains "{domain} \
            --non-interactive --email {DOMAINS_EMAIL} --agree-tos -v"
        "
        """)

        print()
        log.info(f"Certbot set, stopping {SET_HTTPS_SERVER}...")
        print()

        meta.execute_bash_script(f'ssh {remote_host} "docker stop {SET_HTTPS_SERVER} --time 30 | true"')

        log.info(f"{SET_HTTPS_SERVER} stopped, deploy your app/apps of choice!")

    print()
    log.info(f"Https setup for {domain} domain with automatic renewal!")
