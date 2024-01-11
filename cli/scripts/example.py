from argparse import ArgumentParser, SUPPRESS, RawTextHelpFormatter

def args_parser():
    parser = ArgumentParser()
    parser.add_argument("--test", "-t", help="test argument", required=True)
    parser.add_argument("--optional", required=False)
    parser.add_argument("--execute",
                               help="This is potentially dangerous operation, so we require to pass additional flag",
                               required=True, action="store_true")
    
    return parser


args = vars(args_parser().parse_args())

print(f"Test arg: {args}")