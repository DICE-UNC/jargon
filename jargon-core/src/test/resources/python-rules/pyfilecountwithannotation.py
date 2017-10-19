# @RuleEngine="PYTHON"
def testRule(rule_args, callback):                                                                            
    condition = rule_args['*Condition'][1:-1]
    continue_index_old = 1
    size = 0
    count = 0
    inDict = {}
    inDict[PYTHON_MSPARAM_TYPE] = PYTHON_GENQUERYINP_MS_T
    retVal = callback.msiMakeGenQuery('DATA_NAME, DATA_SIZE', condition, inDict)
    inDict = retVal[PYTHON_RE_RET_OUTPUT][2]
    inDict[PYTHON_MSPARAM_TYPE] = PYTHON_GENQUERYINP_MS_T

    outDict = {}
    outDict[PYTHON_MSPARAM_TYPE] = PYTHON_GENQUERYOUT_MS_T
    retVal = callback.msiExecGenQuery(inDict, outDict)
    outDict = retVal[PYTHON_RE_RET_OUTPUT][1]

    dummy = {}
    dummy[PYTHON_MSPARAM_TYPE] = PYTHON_INT_MS_T
    while continue_index_old > 0:
        for row in range(0, int(outDict['rowCnt'])):
            keyStr = 'value_' + str(row) + '_1'
            size = size + int(outDict[keyStr])
            count = count + 1
        continue_index_old = int(outDict['continueInx'])
        if continue_index_old > 0:
            outDict[PYTHON_MSPARAM_TYPE] = PYTHON_GENQUERYOUT_MS_T
            retVal = callback.msiGetMoreRows(inDict, outDict, dummy)
            outDict = retVal[PYTHON_RE_RET_OUTPUT][1]

    callback.writeLine('stdout', 'Number of files in ' + coll + 'is ' + str(count) + 'and total size is ' + str(size))

INPUT *Condition="COLL_NAME like '/tempZone/home/rods/large_coll'"
OUTPUT ruleExecOut