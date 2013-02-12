#Input parameters:
#  Name of *File1  - first output file written by the workflow
#  Name of *File2  - second output file written by the workflow
#Output parameter is:
#  None
#Output from running the example is:
#  message about completion written to stdout
#
# This workflow executes the file called myWorkFlow twice with two different input values
# This is an executable file that is locaed in bin/cmd directory of the iRODS server.
#  It creates an output file using the value given in the second argument. 
# The workflow also prints to stdout the statement about when the execution occurred.
testWorkflow {
	msiExecCmd("myWorkFlow", *File1, "null","null","null",*Result1);
	msiExecCmd("myWorkFlow", *File2, "null","null","null",*Result2);
	msiGetFormattedSystemTime(*myTime,"human","%d-%d-%d %ldh:%ldm:%lds");
	writeLine("stdout", "Workflow Executed Successfully at *myTime");
}


