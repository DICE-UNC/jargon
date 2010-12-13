/*
 * javascript for user group admin functions
 * @author Mike Conway - DICE (www.irods.org)
 */

$(document).ready(function() {
	$("#accordion").accordion("activate", $("#administration"));
	
	$("#user_group_search_type").change(function() {
		var searchType = $("#user_group_search_type").val();
		adjustSearchTypeForm(searchType);
	});

});

function adjustSearchTypeForm(searchType) {

	$("#search_form_by_user_group_name").hide(1000);
	$("#search_form_by_users_in_group").hide(1000);

	if (searchType == "byUserGroupName") {
		$("#search_form_by_user_group_name").show(1000);

	} else if (searchType == "byUserInGroup") {
		$("#search_form_by_users_in_group").show(1000);

	} 

}
