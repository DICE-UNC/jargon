makeThumbnailFromObj
{
	### Make query to get file path on resource  
	
	# Get collection name and object name from path
	msiSplitPath(*objPath,*collName,*objName);

	# Add select field(s) to query input
	msiAddSelectFieldToGenQuery("DATA_PATH", "null", *GenQInp);
	msiAddSelectFieldToGenQuery("RESC_LOC", "null", *GenQInp);
	

	# Add condition(s)
	msiAddConditionToGenQuery("COLL_NAME", "=", *collName, *GenQInp);
	msiAddConditionToGenQuery("DATA_NAME", "=", *objName, *GenQInp);
	msiAddConditionToGenQuery("DATA_RESC_NAME", "=", *resource, *GenQInp);

	# Run query
	msiExecGenQuery(*GenQInp, *GenQOut);
	
	# Extract result
  	foreach (*GenQOut)
	{
		msiGetValByKey(*GenQOut, "DATA_PATH", *data_path);
		msiGetValByKey(*GenQOut, "RESC_LOC", *resc_loc);
	}
	

	### Invoke thumbnail generating script
	msiExecCmd("makeThumbnail.py", "``*data_path''", *resc_loc, "null", "null", *CmdOut);
	msiGetStdoutInExecCmdOut(*CmdOut, *StdoutStr);

	
	#### Testing
	writeLine("stdout", *StdoutStr);

}
INPUT *objPath="/lifelibZone/home/adetorcy/IMG 20110426_165927.jpg", *resource="lifelibResc1"
OUTPUT ruleExecOut