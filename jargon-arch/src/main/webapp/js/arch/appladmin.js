/*
 * javascript for application administration pages
 * 
 * author: Mike Conway - DICE (www.irods.org)
 */

var appsTable;
var context = "/Jargon-arch";

function loadArchivalAppsTable() {
	getUrl = context + "/appladmin/ajax_service_driven_apps_list";
	sendValue(getUrl, "#appl_listing_table_div", "#serviceDrivenAppsTable", "Jargon-arch", justAlert);
}

function justAlert(something) {
	alert("alerted about something");
}

function archiveTableDetailsClick(minMaxIcon) {
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