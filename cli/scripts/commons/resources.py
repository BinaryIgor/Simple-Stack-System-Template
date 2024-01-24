import requests

from . import meta

BASE_URL = "https://api.digitalocean.com/v2"
_API_TOKEN = None
_AUTH_HEADER = None

log = meta.new_log("resources")

TAGS = "tags"
VPCS = "vpcs"
FIREWALLS = "firewalls"
DROPLETS = "droplets"
VOLUMES = "volumes"
VOLUMES_ATTACHMENTS = "volumes-attachments"


def get_droplets(params=None, per_page=100):
    return get(DROPLETS, params=params, per_page=per_page)


def get(resource, params=None, per_page=100):
    _check_token()

    if per_page:
        params = {} if params is None else params
        params["per_page"] = per_page

    response = requests.get(f'{BASE_URL}/{resource}', headers=_AUTH_HEADER, params=params)

    response.raise_for_status()

    return response.json()[resource]


def set_api_token(do_api_token):
    global _API_TOKEN
    _API_TOKEN = do_api_token


def _check_token():
    global _API_TOKEN, _AUTH_HEADER

    if _API_TOKEN and _AUTH_HEADER:
        return

    if _API_TOKEN is None:
        raise Exception("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")

    _AUTH_HEADER = {"Authorization": f"Bearer {_API_TOKEN}"}


def create(resource, data):
    _check_token()
    return requests.post(f"{BASE_URL}/{resource}", headers=_AUTH_HEADER, json=data)


def delete(resource, resource_id):
    _check_token()
    return requests.delete(f"{BASE_URL}/{resource}/{resource_id}", headers=_AUTH_HEADER)
