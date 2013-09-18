trimDataObject {
#Input parameters are:
#  Data object path
#  Optional storage resource name
#  Optional replica number
#  Optional number of replicas to keep
#  Optional administrator flag irodsAdmin, to enable administrator to trim replicas
#Output parameter is:
#  Status
#Output from running the example is:
#  The replicas of File /tempZone/home/rods/sub1/foo2 are deleted
  msiDataObjTrim(*SourceFile,*StorageResource,*ReplicaNumber,*NumberOfReplicasToKeep,*IRODSAdminFlag,*Status);
  writeLine("stdout","The replicas of file *SourceFile are deleted");
}
INPUT *SourceFile="/tempZone/home/rods/sub1/foo2",*StorageResource="null",*ReplicaNumber="null",*NumberOfReplicasToKeep="1",*IRODSAdminFlag="null"
OUTPUT ruleExecOut