ruleHelloWithDelay {
#Input parameters are:
# 
	  
	delay (*DelayInfo) {
	 writeLine("stdout","hello with a delay");
  	print_hello;
	}
  
  }
INPUT  *DelayInfo="<PLUSET>1m</PLUSET><EF>24h</EF>"
OUTPUT ruleExecOut
