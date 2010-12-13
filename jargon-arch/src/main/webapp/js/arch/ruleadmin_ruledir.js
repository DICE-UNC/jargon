/*
 * javascript for rule admin
 * 
 * author: Mike Conway - DICE (www.irods.org)
 */

var appsTable;

/*
 * load the table that lists rule mapping directories
 */
function loadRuleDirs() {
	getUrl = context + "/ruleadmin/ruledir/ajax_rule_dirs_list";
	sendValue(getUrl, "#ruledirListingTableDiv", "#ruleDirsTable", context, clickProcess());
}

/*
 * load the table that lists the contents of the rule mapping directory with the given name
 */
function loadRuleMappingDirContents(data) {
	getUrl = context + "/rulemapping/ajax_rule_mapping_dir_contents";
	sendValue(getUrl, "#ruleMappingListingTableDiv", "#ruleMappingsListTable", context, clickProcess());
}


/*
 * dummy function to pass to load table functions, split out and implement
 */
function clickProcess() {
	
}

function ruleTableDetailsClick(minMaxIcon) {
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