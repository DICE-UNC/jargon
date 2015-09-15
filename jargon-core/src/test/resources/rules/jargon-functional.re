# irods rule config for running PEP functional tests

acPostProcForPut {
 msiString2KeyValPair("postProcForPutFired=true", *meta_kvp);
 msiAssociateKeyValuePairsToObj(*meta_kvp, $objPath, "-d");
} # acPostProcForPut for PEP functional tests