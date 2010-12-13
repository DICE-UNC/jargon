var userTable;
var addUserDialog;

function sendValue(getUrl) {

	prepareForCall();
	if (getUrl.length == 0) {
		getUrl = "/Jargon-lingo/users/ajax_usertable";
	}

	var img = document.createElement('IMG');
	img.setAttribute("src", "/Jargon-lingo/static/images/ajax-loader.gif");

	$("#users_table_div").html(img);

	try {

		$.get(getUrl, function(data) {
			checkAjaxResultForError(data);
			buildUserTable(data);
		}, "html");

	} catch (err) {
		// FIXME: console traces are not good for IE - mcc
		// console.trace();
		$("#users_table_div").html(""); // FIXME: some sort of error icon?

		console.log("error in sendValue():" + err);
	}

}

function closeTableNodes() {
	$(userTable.fnGetNodes()).each(function() {
		if (this.innerHTML.match('circle-minus')) {
			var firstNode = $(this).children().first().children(".ui-icon");
			firstNode.removeClass("ui-icon-circle-minus");
			firstNode.addClass("ui-icon-circle-plus");
			userTable.fnClose(this);
		}

	});
}

/*******************************************************************************
 * Function called by ajax action as response handler. Builds the data table
 * 
 * @param data -
 *            results from ajax call in | delimited format for parsin
 * @return - void
 */
function buildUserTable(data) {

	$("#users_table_div").html(data);
	userTable = $('#usertable').dataTable();
	var newRowNode;

	$('.user_detail_icon', userTable.fnGetNodes()).each(function() {
		$(this).click(function() {
			detailsClick(this);
		});
	});

}

function detailsClick(minMaxIcon) {
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

function closeDetails(minMaxIcon, rowActionIsOn) {
	/* This row is already open - close it */
	minMaxIcon.setAttribute("class", "ui-icon ui-icon-circle-plus");
	userTable.fnClose(rowActionIsOn);
}

function openDetails(minMaxIcon, rowActionIsOn) {
	/* Open this row */
	prepareForCall();
	closeTableNodes();
	// nTr points to row and has id user_id in id
	var userId = $(rowActionIsOn).attr('id');
	var detailsId = "details_" + userId;
	var detailsHtmlDiv = "details_html_" + userId;
	var userDetailsId = "userDetailsForm_" + userId;

	// close other rows
	minMaxIcon.setAttribute("class", "ui-icon ui-icon-circle-minus");
	newRowNode = userTable.fnOpen(rowActionIsOn,
			buildDetailsLayout(userDetailsId), 'details');
	// newRowNode.setAttribute("class", "details");
	newRowNode.setAttribute("id", detailsId);
	askForUserDetailsForm(userDetailsId, userId);

}

function askForUserDetailsForm(userDetailsId, userId) {
	$.get("/Jargon-lingo/users/ajax_user_table_details/" + userId, function(
			userDetailsData) {
		checkAjaxResultForError(userDetailsData);
		displayUserDetails(userDetailsData, userDetailsId, userId);
	}, "html");
}

function buildDetailsLayout(userDetailsId) {
	var td = document.createElement("TD");
	td.setAttribute("colspan", "4");

	var userDetailsFormDiv = document.createElement("DIV");
	userDetailsFormDiv.setAttribute("id", userDetailsId);
	userDetailsFormDiv.setAttribute("class", "detailsUpdateForm");

	td.appendChild(userDetailsFormDiv);

	return $(td).html();
}

/* Formating function for row details FIXME: old */
function showDetailsProcessingIcon() {
	var div = document.createElement('DIV');
	var img = document.createElement('IMG');
	img.setAttribute("src", "/Jargon-lingo/static/images/ajax-loader.gif");
	div.appendChild(img);
	var htmlVal = div.innerHTML;
	return htmlVal;
}

function displayUserDetails(data, detailsId, userId, isUpdateDisplay) {

	try {
		checkAjaxResultForErrorAndDisplayInGivenArea(data,
				"#details_javascript_message_area");
		var detailsIdSelector = "#" + detailsId;
		$(detailsIdSelector).html(data);
		setupDetailsEvents(userId, detailsId);
		if (isUpdateDisplay == true) {
			alert("update successful");
		}
	} catch (err) {
		alert(err);
	}

}

/*
 * Process an update to the user details caused by clicking the update button.
 * Sends an AJAX post of the user form
 */
function updateDetailUser(userId, detailsId) {
	var detailsIdSelector = "#" + detailsId;
	var updateForm = $("#user_details_update_form");
	var url = updateForm.attr("action");
	var elements = updateForm.find(":input");
	var formParams = $.param(elements);
	$.post(url, formParams, function(responseText) {
		displayUserDetails(responseText, detailsId, userId, true);
	}, "html");
}

function deleteDetailUser() {
	alert("deleting user");

}

function setupDetailsEvents(userId, detailsId) {
	$("#update_detail_user").click(function() {
		updateDetailUser(userId, detailsId);
	});
	$("#delete_detail_user").click(function() {
		deleteDetailUser(userId, detailsId);
	});
	$("#new_detail_user").click(function() {
        $("#new_detail_user").removeClass("ui-state-active");
		addUserDialog.dialog('open');

	});
}

/*
 * search for the user given a user name pattern
 */
function searchByUserName() {
	var userName = $("#username").val();
	getUrl = "/Jargon-lingo/users/ajax_user_search_user_name?userName="
			+ userName;
	sendValue(getUrl);
}

/*
 * search for the user given a user name pattern
 */
function searchByUserGroupName() {
	var userGroupName = $("#usergroupname").val();
	getUrl = "/Jargon-lingo/users/ajax_user_search_user_group?userGroup="
			+ userGroupName;
	sendValue(getUrl);
}

/*
 * search for the user given a user name pattern
 */
function searchByUserType() {
	var userType = $("#user_type").val();
	getUrl = "/Jargon-lingo/users/ajax_user_search_user_type?userType="
			+ userType;
	sendValue(getUrl);
}

function adjustSearchTypeForm(searchType) {

	$("#search_form_by_user_name").hide(1000);
	$("#search_form_by_user_group").hide(1000);
	$("#search_form_by_user_type").hide(1000);

	if (searchType == "byUserName") {
		$("#search_form_by_user_name").show(1000);

	} else if (searchType == "byUserGroup") {
		$("#search_form_by_user_group").show(1000);

	} else if (searchType == "byUserType") {
		$("#search_form_by_user_type").show(1000);
	}

}

/*
 * Function called when the add user dialog needs to be shown
 */
function displayAddUser() {
	// make an ajax call to get the user add form
	lcSendValueAndCallbackHtmlAfterErrorCheck("/users/add", "nothing", "nothing", displayAddUserAjaxCallback);
}

/*
 * When the add user ajax response for the initial form returns, plug it into a div and then make it a dialog
 */
function displayAddUserAjaxCallback(data) {
	$("#detail_activity_div").html(data);
	var dialogOpts = {
			modal : true,
			width : 800,
			height : 800,
			autoOpen : true,
			title : '<h3>Add A User</h3>'
		};
		
		addUserDialog = $("#div_for_user_add_form").dialog(dialogOpts);

}



$(document).ready(function() {
	$("#accordion").accordion("activate", 1);

	var dialogOpts = {
		modal : true,
		width : 800,
		height : 800,
		autoOpen : false,
		title : '<h3>Add A User</h3>'
	};
	
	//addUserDialog = $("#div_for_user_add_form").dialog(dialogOpts);

	$("#user_search_type").change(function() {
		var searchType = $("#user_search_type").val();
		adjustSearchTypeForm(searchType);
	});

	$("#search_users_by_user_name").click(function() {
		searchByUserName();
	});
	$("#search_users_by_user_group").click(function() {
		searchByUserGroupName();
	});
	$("#search_users_by_user_type").click(function() {
		searchByUserType();
	});

	$("#search_form_by_user_name").show(1000);

	sendValue("");
	
	$("#add_user").click(function() {
		displayAddUser();
	});

});