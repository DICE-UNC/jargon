testRuleBug1641 {
# Input parameters are:
#   Data object path
#   Flags in form keyword=value
#    localPath
#    rescName
#    replNum
#    numThreads
#    forceFlag
#    verifyChksum
# Output parameter is
#   Status
# Output from running the example is:
#  File /tempZone/home/rods/sub1/foo1 is retrieved from the data grid
#  msiSplitPath(*SourceFile1,*Coll,*File);
  msiDataObjGet(*SourceFile1,"localPath=*localPath1++++forceFlag=",*Status);
  writeLine("stdout","File *SourceFile1 is retrieved from the data grid");
  msiDataObjPut(*DestFile,*DestResource,"localPath=*putLocalSource++++forceFlag=",*Status);
  writeLine("stdout","File *DestFile is uploaded to the data grid");
  msiDataObjGet(*SourceFile2,"localPath=*localPath2++++forceFlag=",*Status);
  writeLine("stdout","File *SourceFile2 is retrieved from the data grid");
}
INPUT *SourceFile1="/tempZone/home/rods/sub1/foo1",*localPath1="/blah",*SourceFile2="/tempZone/home/rods/sub1/foo1",*localPath2="/blah",*DestFile="/blah",*DestResource="test1-resc",*putLocalSource="/blah"
OUTPUT ruleExecOut,*DestFile


