def myTestRule(rule_args, callback, rei):
    path = global_vars['*Path'][1:-1]
    acl = global_vars['*Acl'][1:-1]

    ret_val = callback.msiCheckAccess(path, acl, 0)
    result = ret_val['arguments'][2]
    if not result:
        callback.writeLine('stdout', 'File ' + path + ' does not have access ' + acl)
    else:
        callback.writeLine('stdout', 'File ' + path + ' has access ' + acl)

INPUT *Path="/tempZone/home/rods/sub1/foo1", *Acl="own"
OUTPUT ruleExecOut