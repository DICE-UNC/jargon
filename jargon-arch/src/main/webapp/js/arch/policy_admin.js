/*
 * javascript for policyAdmin
 * 
 * author: Mike Conway - DICE (www.irods.org)
 */

/*
 * load the table that lists rule mapping directories
 */
function loadPolicyDirs() {
	getUrl = context + "/policyadmin/policydir/ajax_dirs_list";
	sendValue(getUrl, "#policyRepositoryTableDiv", "#policyDirsTable", context,
			clickProcess());
}

/*
 * load the table that lists the contents of the policy mapping directory with
 * the given name
 */
function loadPolicyDirContents(repositoryName) {
	if (repositoryName.length > 0) {
		getUrl = context
				+ "/policyadmin/policydir/ajax_policy_dir_contents?policyName="
				+ encodeURI(repositoryName);
		sendValue(getUrl, "#policyListingTableDiv", "#policyListingTable",
				context, clickProcess());
	}
}

/*
 * dummy function to pass to load table functions, split out and implement
 */
function clickProcess() {

}

function policyTableDetailsClick(minMaxIcon) {
	var nTr = minMaxIcon.parentNode.parentNode;

	if (minMaxIcon.parentNode.innerHTML.match('circle-minus')) {
		closeDetails(minMaxIcon, nTr);
	} else {
		try {
			openDetails(minMaxIcon, nTr);
		} catch (err) {
			console.log("error in getMetaData():" + err);
		}

	}

}