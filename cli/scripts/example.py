from commons import meta, crypto, infra

log = meta.new_log("example")
args = meta.cmd_args({
    "test": {
        "help": "Test argument",
        "required": True
    },
    "some_optional_arg": {
        "help": "Optional flag",
        "action": "store_false"}
}, script_description="""
   Multiline script description.
    
    It even has some HTML:
    <div>
      <p>Testing html aesthetics</p>
    </div>
""")

log.info(crypto.random_key())
log.info(crypto.random_password())
log.info(infra.droplets())