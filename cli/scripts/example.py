from commons import meta

log = meta.new_log("example")
args = meta.cmd_args({
    "test": {
        "help": "Test argument",
        "required": True
    },
    "some_optional_arg": {
        "help": "Optional flag",
        "action": "store_false"}
}, env_arg=False, script_description="""
   Multiline script description.
    
    It even has some HTML:
    <div>
      <p>Testing html aesthetics</p>
    </div>
""")

log.info(f"Test args: {args}")
log.info(f"Root 1: {meta.cli_root_dir()}")
log.info(f"Root 2: {meta.root_dir()}")
log.info(f"Root 3: {meta.root_src_dir()}")
log.info(f"File 4: {meta.env_config()}")
log.info("")
log.info(f"Apps: {meta.sorted_apps()}")
