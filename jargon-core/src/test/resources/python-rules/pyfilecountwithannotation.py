# @RuleEngine="PYTHON"
@external
def myTestRule(rule_args, callback, rei):
    dest = global_vars['*dest'][1:-1]
    src = global_vars['*source'][1:-1]

    callback.writeLine('stdout', 'dest is ' + dest)
    callback.writeLine('stdout', 'source is ' + src)
    dest += src
    callback.writeLine('stdout', 'dest is now ' + dest)

INPUT *dest="foo", *source="bar"
OUTPUT ruleExecOut