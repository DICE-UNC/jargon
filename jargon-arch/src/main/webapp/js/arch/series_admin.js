/*
 * javascript for seriesAdmin
 * 
 * author: Mike Conway - DICE (www.irods.org)
 */


/*
 * load the table that lists the contents of the policy mapping directory with
 * the given name
 */
function loadSeriesTable(serviceDrivenAppName) {
	if (serviceDrivenAppName.length > 0) {
		getUrl = context
				+ "/seriesadmin/seriesdir/ajax_series_dir_contents?policyDrivenServiceName="
				+ encodeURI(serviceDrivenAppName);
		sendValue(getUrl, "#seriesListingTableDiv", "#seriesListingTable",
				context, clickProcess());
	}
}

/*
 * dummy function to pass to load table functions, split out and implement
 */
function clickProcess() {
	// nothing now
}


function containingServiceNameClick(data) {
	serviceDrivenAppName = data;
	getUrl = context
	+ "/seriesadmin/seriesdir/ajax_policy_options_for_service?policyDrivenServiceName="
	+ encodeURI(serviceDrivenAppName);
	sendValueAndCallbackHtmlAfterErrorCheck(getUrl,"#verticalForm", "", afterPolicyComboFilledIn);
	
}

function afterPolicyComboFilledIn(data) {
	// just get the  <options>

	begin = data.indexOf("<option>");
	end = data.indexOf("</span>");
	policyOptions = data.substring(begin, end);
	$("#boundPolicyName").html(policyOptions);
}




