 myTestRule  {
 writeString("stdout", "before transfer");
 msiDataObjGet(*SourceFile,"localPath=*localPath++++rescName=test1-resc++++forceFlag=",*status)
 writeString("stdout", "after transfer");
 writeString("stderr", "Error:blah");
}
INPUT *SourceFile="/tempZone/home/rods/sub1/foo1",*localPath="/blah"
OUTPUT ruleExecOut